/**
 *
 */
package br.com.swconsultoria.nfe.util;

import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.ServicosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import lombok.extern.java.Log;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration; // Added
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator; // Added
import java.util.logging.Logger;

/**
 * @author Samuel Oliveira
 *
 * Classe responsávelem montar as URL's de consulta de serviços do SEFAZ.
 */
@Log
public class WebServiceUtil {

    private final static Logger logger = Logger.getLogger(WebServiceUtil.class.getName());

    private static String getStringIgnoreCase(SubnodeConfiguration sectionConfig, String targetKey) {
        if (sectionConfig == null || sectionConfig.isEmpty() || targetKey == null) {
            return null;
        }
        // First, try direct access with the targetKey, relying on SubnodeConfiguration's case-insensitivity
        String value = sectionConfig.getString(targetKey, null);
        if (value != null) {
            // log.fine("getStringIgnoreCase: Direct lookup for targetKey '" + targetKey + "' found value."); // Optional fine log
            return value;
        }

        // If direct access failed, iterate and check with equalsIgnoreCase
        for (Iterator<String> it = sectionConfig.getKeys(); it.hasNext(); ) {
            String key = it.next();
            String normalizedIteratedKey = key.replace("..", "."); // Normalize ".." to "." from iterated key
            if (targetKey.equalsIgnoreCase(normalizedIteratedKey)) {
                // log.fine("getStringIgnoreCase: Iterated match found for targetKey '" + targetKey + "' with INI key '" + key + "' (normalized to '" + normalizedIteratedKey + "')."); // Optional fine log
                return sectionConfig.getString(key); // Use original iterated key
            }
        }
        // log.fine("getStringIgnoreCase: No match found for targetKey '" + targetKey + "'."); // Optional fine log
        return null;
    }

    /**
     * Retorna a URL para consulta de operações do SEFAZ.<br>
     *
     * <p>
     * O método carrega o arquivo <b>WebServicesNfe.ini</b> que contêm as
     * URL's de operações do SEFAZ, busca pela seção no arquivo .ini que
     * corresponda com os argumentos <b>tipo</b>, <b>config</b>, <b>servico</b>
     * e retorna essa URL.
     * </p>
     *
     * @param config interface que contêm os dados necessários para a comunicação.
     * @param tipoDocumento DocumentoEnum.NFE e ConstantesUtil.NFCE
     * @param tipoServico é a operação que se deseja fazer.<br>
     * Ex.: para consultas status deserviço no ambiente de produção
     * use ServicosEnum.NfeStatusServico_4.00
     *
     * @return url String que representa a URL do serviço.
     * @throws NfeException
     *
     * @see ConfiguracoesNfe
     * @see ConstantesUtil
     **/
    public static String getUrl(ConfiguracoesNfe config, DocumentoEnum tipoDocumento, ServicosEnum tipoServico) throws NfeException {

        try {

            String secao = tipoDocumento.getTipo() + "_" + config.getEstado() + "_"
                    + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");

            InputStream is;
            if (ObjetoUtil.verifica(config.getArquivoWebService()).isPresent()) {
                File arquivo = new File(config.getArquivoWebService());
                if (!arquivo.exists())
                    throw new FileNotFoundException("Arquivo WebService" + config.getArquivoWebService() + " não encontrado");
                is = new FileInputStream(arquivo);
                log.info("[ARQUIVO INI CUSTOMIZADO]: " + config.getArquivoWebService());
            } else {
                is = WebServiceUtil.class.getResourceAsStream("/WebServicesNfe.ini");
            }

            INIConfiguration iniConfig = new INIConfiguration();
            // It's important to use a Reader with INIConfiguration's read method.
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                iniConfig.read(reader);
            } finally {
                // Ensure 'is' is closed if it was not from getResourceAsStream or if reader doesn't close it.
                // If 'is' is from getResourceAsStream, it's generally managed by the class loader, but closing doesn't hurt.
                // If 'is' is a FileInputStream, it MUST be closed.
                // The InputStreamReader try-with-resources should close 'is', so this might be redundant but safe.
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        // Log or ignore, as we are already in a try-catch block for NfeException
                        log.fine("Error closing InputStream: " + e.getMessage());
                    }
                }
            }

            // The variable 'secao' from the outer scope (first line in try block) is the true original section name.
            // We'll use 'lookupSectionKey' for the actual key to use for INI section lookups,
            // which might be modified by 'Usar', SVC, or AN logic.
            String lookupSectionKey = secao; // Initialize with the original 'secao'

            SubnodeConfiguration initialSectionConfig = iniConfig.getSection(lookupSectionKey);
            String usarValue = null;
            if (initialSectionConfig != null && !initialSectionConfig.isEmpty()) {
                usarValue = getStringIgnoreCase(initialSectionConfig, "Usar");
            }

            String finalUrl = null;

            if (tipoServico.equals(ServicosEnum.CONSULTA_CADASTRO) && (
                    config.getEstado().equals(EstadosEnum.PA) ||
                            config.getEstado().equals(EstadosEnum.AM) ||
                            config.getEstado().equals(EstadosEnum.AL) ||
                            config.getEstado().equals(EstadosEnum.AP) ||
                            config.getEstado().equals(EstadosEnum.DF) ||
                            config.getEstado().equals(EstadosEnum.PI) ||
                            config.getEstado().equals(EstadosEnum.RJ) ||
                            config.getEstado().equals(EstadosEnum.RO) ||
                            config.getEstado().equals(EstadosEnum.SE) ||
                            config.getEstado().equals(EstadosEnum.TO))) {
                throw new NfeException("Estado não possui Consulta Cadastro.");
            } else if (tipoServico.equals(ServicosEnum.DISTRIBUICAO_DFE) ||
                    tipoServico.equals(ServicosEnum.MANIFESTACAO) ||
                    tipoServico.equals(ServicosEnum.EPEC)) {
                lookupSectionKey = config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "NFe_AN_H" : "NFe_AN_P";
                SubnodeConfiguration nationalSectionConfig = iniConfig.getSection(lookupSectionKey);
                // log.info("Looking for National Service key: '" + tipoServico.getServico() + "' in section '" + lookupSectionKey + "'"); // Optional fine log
                finalUrl = getStringIgnoreCase(nationalSectionConfig, tipoServico.getServico());
            } else if (!tipoServico.equals(ServicosEnum.URL_CONSULTANFCE) &&
                    !tipoServico.equals(ServicosEnum.URL_QRCODE) &&
                    config.isContigenciaSVC() && tipoDocumento.equals(DocumentoEnum.NFE)) {
                if (config.getEstado().equals(EstadosEnum.GO) || config.getEstado().equals(EstadosEnum.AM) ||
                        config.getEstado().equals(EstadosEnum.BA) || config.getEstado().equals(EstadosEnum.CE) ||
                        config.getEstado().equals(EstadosEnum.MA) || config.getEstado().equals(EstadosEnum.MS) ||
                        config.getEstado().equals(EstadosEnum.MT) || config.getEstado().equals(EstadosEnum.PA) ||
                        config.getEstado().equals(EstadosEnum.PE) || config.getEstado().equals(EstadosEnum.PI) ||
                        config.getEstado().equals(EstadosEnum.PR)) {
                    lookupSectionKey = tipoDocumento.getTipo() + "_SVRS_" + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");
                } else {
                    lookupSectionKey = tipoDocumento.getTipo() + "_SVC-AN_" + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");
                }
                SubnodeConfiguration svcSectionConfig = iniConfig.getSection(lookupSectionKey);
                // log.info("Looking for SVC service key: '" + tipoServico.getServico() + "' in section '" + lookupSectionKey + "'"); // Optional fine log
                finalUrl = getStringIgnoreCase(svcSectionConfig, tipoServico.getServico());
            } else if (ObjetoUtil.verifica(usarValue).isPresent() &&
                    !tipoServico.equals(ServicosEnum.URL_CONSULTANFCE) &&
                    !tipoServico.equals(ServicosEnum.URL_QRCODE)) {
                lookupSectionKey = usarValue;
                SubnodeConfiguration usarRedirectedSectionConfig = iniConfig.getSection(lookupSectionKey);
                // log.info("Looking for service key: '" + tipoServico.getServico() + "' in 'Usar' redirected section '" + lookupSectionKey + "'"); // Optional fine log
                finalUrl = getStringIgnoreCase(usarRedirectedSectionConfig, tipoServico.getServico());
            } else {
                // Default case: lookupSectionKey is already the initial 'secao' (or was not overridden by 'Usar' for QR/Consulta)
                SubnodeConfiguration currentSectionConfigToUse = iniConfig.getSection(lookupSectionKey);
                // log.info("Looking for service key: '" + tipoServico.getServico() + "' in section '" + lookupSectionKey + "'"); // Optional fine log
                finalUrl = getStringIgnoreCase(currentSectionConfigToUse, tipoServico.getServico());
            }

            final String finalLookupSectionKeyForLambda = lookupSectionKey; // Essential for lambda
            ObjetoUtil.verifica(finalUrl).orElseThrow(() -> new NfeException(
                    "WebService de " + tipoServico + " não encontrado para " + config.getEstado().getNome() + " na seção " + finalLookupSectionKeyForLambda));

            log.info("[URL]: " + tipoServico + ": " + finalUrl);
            return finalUrl;

        } catch (ConfigurationException e) {
            throw new NfeException("Erro ao ler arquivo de configuraçãoWebService: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new NfeException(e.getMessage(),e);
        }

    }
}
