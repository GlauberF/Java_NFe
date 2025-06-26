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
// Apache Commons Configuration imports removed
// import org.apache.commons.configuration2.INIConfiguration;
// import org.apache.commons.configuration2.SubnodeConfiguration;
// import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator; // Will be removed if getStringIgnoreCase and determineLookupSectionKey are fully removed/simplified
import java.util.logging.Logger;
// Import for new Map-based properties
import java.util.Map;
import java.util.HashMap; // Added for parseIniFile
import java.util.Properties; // Kept for now, might be used or removed later
import java.util.regex.Matcher; // Added for parseIniFile
import java.util.regex.Pattern; // Added for parseIniFile


/**
 * @author Samuel Oliveira
 *
 * Classe responsávelem montar as URL's de consulta de serviços do SEFAZ.
 */
@Log
public class WebServiceUtil {

    private final static Logger logger = Logger.getLogger(WebServiceUtil.class.getName());
    private static final Pattern sectionPattern = Pattern.compile("^\\[(.+)\\]$");

    private static String getIniValueIgnoreCase(Map<String, String> sectionMap, String targetKey) {
        // Removed logger parameter as class logger can be used if needed, or keep logging minimal
        if (sectionMap == null || sectionMap.isEmpty() || targetKey == null) {
            return null;
        }
        // logger.info("getIniValueIgnoreCase: TargetKey='" + targetKey + "'. Iterating section keys.");
        for (Map.Entry<String, String> entry : sectionMap.entrySet()) {
            String keyFromIni = entry.getKey();
            // Normalize both keys for comparison to handle potential inconsistencies from parser or INI file
            String normalizedKeyFromIni = keyFromIni.replace("..", ".");
            String normalizedTargetKey = targetKey.replace("..", "."); // Should not happen from Enum, but defensive
            // logger.info("  - Comparing targetKey '" + normalizedTargetKey + "' with INI key (normalized): '" + normalizedKeyFromIni + "' (original iterated: '" + keyFromIni + "')");
            if (normalizedTargetKey.equalsIgnoreCase(normalizedKeyFromIni)) {
                // logger.info("  - Match found! Returning value for original INI key '" + keyFromIni + "'");
                return entry.getValue();
            }
        }
        // logger.info("getIniValueIgnoreCase: No match found for TargetKey='" + targetKey + "'.");
        return null;
    }

    private static Map<String, Map<String, String>> parseIniFile(InputStream inputStream) throws IOException, NfeException {
        Map<String, Map<String, String>> iniData = new HashMap<>();
        String currentSectionName = null;
        Map<String, String> currentSectionMap = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                    continue; // Skip empty lines and comments
                }

                Matcher sectionMatcher = sectionPattern.matcher(line);
                if (sectionMatcher.matches()) {
                    // If currentSectionMap is not null and not empty, it means a previous section was being processed.
                    // It's already in iniData, as we put it there when its name was found.
                    currentSectionName = sectionMatcher.group(1).trim();
                    if (currentSectionName.isEmpty()) {
                        throw new NfeException("Nome da seção inválido (vazio) no arquivo INI.");
                    }
                    // Ensure new section map is created, even if previous one with same name existed (though INI typically doesn't repeat sections)
                    currentSectionMap = new HashMap<>();
                    iniData.put(currentSectionName, currentSectionMap);
                } else {
                    if (currentSectionName == null) {
                        // Property outside of any section - not expected for WebServicesNfe.ini
                        // For now, we can log and ignore, or throw an exception.
                        // Based on prompt, let's be strict for this specific INI structure.
                        throw new NfeException("Propriedade encontrada fora de uma seção: " + line);
                    }

                    int separatorPos = -1;
                    int equalsPos = line.indexOf('=');
                    // According to INI standards, some parsers also accept ':' but '=' is more common.
                    // The original ini4j might have handled both. For this custom parser, let's stick to '=' for simplicity
                    // unless ':' is confirmed to be used in WebServicesNfe.ini for key-value.
                    // A quick check of WebServicesNfe.ini shows only '='.
                    separatorPos = equalsPos;

                    if (separatorPos != -1) {
                        String key = line.substring(0, separatorPos).trim();
                        String value = line.substring(separatorPos + 1).trim();
                        if (!key.isEmpty() && currentSectionMap != null) {
                            currentSectionMap.put(key, value);
                        } else if (key.isEmpty()){
                            logger.warning("Linha malformada (chave vazia): " + line);
                        } else {
                             // currentSectionMap should not be null here if currentSectionName is set.
                             // This case implies currentSectionName was set, but currentSectionMap wasn't put in iniData or was null.
                             // This should ideally not happen if logic is correct.
                            logger.warning("Tentativa de adicionar propriedade a uma seção nula: " + line);
                        }
                    } else {
                        // Line is not a comment, not a section, and not a valid key-value pair.
                        logger.warning("Linha malformada ignorada: " + line);
                    }
                }
            }
        }
        return iniData;
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
        InputStream is = null;
        Map<String, Map<String, String>> iniData;
        try {
            if (ObjetoUtil.verifica(config.getArquivoWebService()).isPresent()) {
                File arquivo = new File(config.getArquivoWebService());
                if (!arquivo.exists()) {
                    throw new FileNotFoundException("Arquivo WebService " + config.getArquivoWebService() + " não encontrado");
                }
                is = new FileInputStream(arquivo);
                logger.info("[ARQUIVO INI CUSTOMIZADO]: " + config.getArquivoWebService());
            } else {
                is = WebServiceUtil.class.getResourceAsStream("/WebServicesNfe.ini");
                if (is == null) {
                    throw new NfeException("Arquivo WebServicesNfe.ini não encontrado no classpath.");
                }
            }
            iniData = parseIniFile(is);
        } catch (IOException e) {
            throw new NfeException("Erro ao carregar arquivo de configuração WebService: " + e.getMessage(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.fine("Erro ao fechar InputStream: " + e.getMessage());
                }
            }
        }

        String initialSecaoKey = tipoDocumento.getTipo() + "_" + config.getEstado() + "_"
                + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");

        String lookupSectionKey = initialSecaoKey;
        Map<String, String> initialSectionMap = iniData.get(initialSecaoKey);
        String usarValue = getIniValueIgnoreCase(initialSectionMap, "Usar");

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
            Map<String, String> nationalSectionMap = iniData.get(lookupSectionKey);
            finalUrl = getIniValueIgnoreCase(nationalSectionMap, tipoServico.getServico());
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
            Map<String, String> svcSectionMap = iniData.get(lookupSectionKey);
            finalUrl = getIniValueIgnoreCase(svcSectionMap, tipoServico.getServico());
        } else if (ObjetoUtil.verifica(usarValue).isPresent() &&
                !tipoServico.equals(ServicosEnum.URL_CONSULTANFCE) &&
                !tipoServico.equals(ServicosEnum.URL_QRCODE)) {
            lookupSectionKey = usarValue;
            Map<String, String> usarRedirectedSectionMap = iniData.get(lookupSectionKey);
            finalUrl = getIniValueIgnoreCase(usarRedirectedSectionMap, tipoServico.getServico());
        } else {
            Map<String, String> currentSectionMap = iniData.get(lookupSectionKey);
            finalUrl = getIniValueIgnoreCase(currentSectionMap, tipoServico.getServico());
        }

        final String finalLookupSectionKeyForLambda = lookupSectionKey;
        ObjetoUtil.verifica(finalUrl).orElseThrow(() -> new NfeException(
                "WebService de " + tipoServico + " não encontrado para " + config.getEstado().getNome() + " na seção " + finalLookupSectionKeyForLambda));

        logger.info("[URL]: " + tipoServico + ": " + finalUrl);
        return finalUrl;
    }
}
