/**
 *
 */
package br.com.swconsultoria.nfe.util;

import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schema.consCad.TConsCad;
import br.com.swconsultoria.nfe.schema.distdfeint.DistDFeInt;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento;
import br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento;
import br.com.swconsultoria.nfe.schema_4.consReciNFe.TConsReciNFe;
import br.com.swconsultoria.nfe.schema_4.consSitNFe.TConsSitNFe;
import br.com.swconsultoria.nfe.schema_4.consStatServ.TConsStatServ;
import br.com.swconsultoria.nfe.schema_4.enviNFe.*;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TInutNFe;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TProcInutNFe;
import br.com.swconsultoria.nfe.schema_4.inutNFe.TRetInutNFe;
import br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TRetConsSitNFe;
import lombok.extern.java.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.StringJoiner;
import java.util.zip.GZIPInputStream;

/**
 * Classe Responsavel por Metodos referentes ao XML
 *
 * @author Samuel Oliveira
 */
@Log
public class XmlNfeUtil {

    private static final String STATUS = "TConsStatServ";
    private static final String SITUACAO_NFE = "TConsSitNFe";
    private static final String ENVIO_NFE = "TEnviNFe";
    private static final String DIST_DFE = "DistDFeInt";
    private static final String INUTILIZACAO = "TInutNFe";
    private static final String NFEPROC = "TNfeProc";
    private static final String NFE = "TNFe";
    private static final String EVENTO = "TEnvEvento";
    private static final String TPROCEVENTO = "TProcEvento";
    private static final String TCONSRECINFE = "TConsReciNFe";
    private static final String TCONS_CAD = "TConsCad";
    private static final String TPROCINUT = "TProcInutNFe";
    private static final String RETORNO_ENVIO = "TRetEnviNFe";
    private static final String SITUACAO_NFE_RET = "TRetConsSitNFe";
    private static final String RET_RECIBO_NFE = "TRetConsReciNFe";
    private static final String RET_STATUS_SERVICO = "TRetConsStatServ";
    private static final String RET_CONS_CAD = "TRetConsCad";
    private static final String RET_DIST_DFE = "RetDistDFeInt";
    private static final String RET_ENV_EVENTO = "TRetEnvEvento";
    private static final String RET_INUT_NFE = "TRetInutNFe";
    private static final String TPROCCANCELAR = "br.com.swconsultoria.nfe.schema.envEventoCancNFe.TProcEvento";
    private static final String TPROCATORINTERESSADO = "br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TProcEvento";
    private static final String TPROCCANCELARSUBST = "br.com.swconsultoria.nfe.schema.envEventoCancSubst.TProcEvento";
    private static final String TPROCCCE = "br.com.swconsultoria.nfe.schema.envcce.TProcEvento";
    private static final String TPROCEPEC = "br.com.swconsultoria.nfe.schema.envEpec.TProcEvento";
    private static final String TPROCMAN = "br.com.swconsultoria.nfe.schema.envConfRecebto.TProcEvento";
    private static final String TPROCINSUCESSO = "br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TProcEvento";
    private static final String TPROCCANCINSUCESSO = "br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TProcEvento";
    private static final String TPROCECONF = "br.com.swconsultoria.nfe.schema.envEventoEConf.TProcEvento";
    private static final String TPROCCANCECONF = "br.com.swconsultoria.nfe.schema.envEventoCancEConf.TProcEvento";
    private static final String TProtNFe = "TProtNFe";
    private static final String TProtEnvi = "br.com.swconsultoria.nfe.schema_4.enviNFe.TProtNFe";
    private static final String TProtCons = "br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe";
    private static final String TProtReci = "br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe";
    private static final String CANCELAR = "br.com.swconsultoria.nfe.schema.envEventoCancNFe.TEnvEvento";
    private static final String ATOR_INTERESSADO = "br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TEnvEvento";
    private static final String INSUCESSO_ENTREGA = "br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TEnvEvento";
    private static final String CANC_INSUCESSO_ENTREGA = "br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TEnvEvento";
    private static final String ECONF = "br.com.swconsultoria.nfe.schema.envEventoEConf.TEnvEvento";
    private static final String CANC_ECONF = "br.com.swconsultoria.nfe.schema.envEventoCancEConf.TEnvEvento";
    private static final String CANCELAR_SUBSTITUICAO = "br.com.swconsultoria.nfe.schema.envEventoCancSubst.TEnvEvento";
    private static final String CCE = "br.com.swconsultoria.nfe.schema.envcce.TEnvEvento";
    private static final String EPEC = "br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento";
    private static final String MANIFESTAR = "br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento";
    private static final String RET_CANCELAR = "br.com.swconsultoria.nfe.schema.envEventoCancNFe.TRetEnvEvento";
    private static final String RET_ATOR_INTERESSADO = "br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TRetEnvEvento";
    private static final String RET_INSUCESSO_ENTREGA = "br.com.swconsultoria.nfe.schema.retEventoInsucessoNFe.TRetEnvEvento";
    private static final String RET_CANC_INSUCESSO_ENTREGA = "br.com.swconsultoria.nfe.schema.retEventoCancInsucessoNFe.TRetEnvEvento";
    private static final String RET_ECONF = "br.com.swconsultoria.nfe.schema.retEventoEConf.TRetEnvEvento";
    private static final String RET_CANC_ECONF = "br.com.swconsultoria.nfe.schema.retEventoCancEConf.TRetEnvEvento";
    private static final String RET_CANCELAR_SUBSTITUICAO = "br.com.swconsultoria.nfe.schema.envEventoCancSubst.TRetEnvEvento";
    private static final String RET_CCE = "br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento";
    private static final String RET_EPEC = "br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento";
    private static final String RET_MANIFESTAR = "br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento";

    private XmlNfeUtil() {}

    /**
     * Transforma o String do XML em Objeto
     *
     * @param xml
     * @param classe
     * @return T
     */
    public static <T> T xmlToObject(String xml, Class<T> classe) throws JAXBException {
        return JAXB.unmarshal(new StreamSource(new StringReader(xml)), classe);
    }

    /**
     * Transforma o Objeto em XML(String)
     *
     * @param obj
     * @return
     * @throws JAXBException
     * @throws NfeException
     */
    public static <T> String objectToXml(Object obj) throws JAXBException, NfeException {
        return objectToXml(obj, Charset.forName("UTF-8"));
    }

    public static <T> String objectToXml(Object obj, Charset encode) throws JAXBException, NfeException {

        JAXBContext context;
        JAXBElement<?> element;
        //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
        boolean layoutReforma = obj.getClass().getName().contains("schema_rt");

        switch (obj.getClass().getSimpleName()) {

            case STATUS:
                context = JAXBContext.newInstance(TConsStatServ.class);
                element = new br.com.swconsultoria.nfe.schema_4.consStatServ.ObjectFactory().createConsStatServ((TConsStatServ) obj);
                break;

            case ENVIO_NFE:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TEnviNFe.class);
                    element = XsdUtil.NfeRt.createTEnviNFe((br.com.swconsultoria.nfe.schema_rt.nfe.TEnviNFe) obj);
                } else {
                    context = JAXBContext.newInstance(TEnviNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.enviNFe.ObjectFactory().createEnviNFe((TEnviNFe) obj);
                }
                break;

            case RETORNO_ENVIO:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TRetEnviNFe.class);
                    element = XsdUtil.NfeRt.createTRetEnviNFe((br.com.swconsultoria.nfe.schema_rt.nfe.TRetEnviNFe) obj);
                } else {
                    context = JAXBContext.newInstance(TRetEnviNFe.class);
                    element = XsdUtil.enviNfe.createTRetEnviNFe((TRetEnviNFe) obj);
                }
                break;

            case SITUACAO_NFE:
                context = JAXBContext.newInstance(TConsSitNFe.class);
                element = new br.com.swconsultoria.nfe.schema_4.consSitNFe.ObjectFactory().createConsSitNFe((TConsSitNFe) obj);
                break;

            case DIST_DFE:
                context = JAXBContext.newInstance(DistDFeInt.class);
                element = XsdUtil.distDFeInt.createDistDFeInt((DistDFeInt) obj);
                break;

            case TCONSRECINFE:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TConsReciNFe.class);
                    element = XsdUtil.NfeRt.createTConsReciNFe((br.com.swconsultoria.nfe.schema_rt.nfe.TConsReciNFe) obj);
                } else {
                    context = JAXBContext.newInstance(TConsReciNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.consReciNFe.ObjectFactory().createConsReciNFe((TConsReciNFe) obj);
                }
                break;

            case TCONS_CAD:
                context = JAXBContext.newInstance(TConsCad.class);
                element = new br.com.swconsultoria.nfe.schema.consCad.ObjectFactory().createConsCad((TConsCad) obj);
                break;

            case INUTILIZACAO:
                context = JAXBContext.newInstance(TInutNFe.class);
                element = new br.com.swconsultoria.nfe.schema_4.inutNFe.ObjectFactory().createInutNFe((TInutNFe) obj);
                break;

            case RET_INUT_NFE:
                context = JAXBContext.newInstance(TRetInutNFe.class);
                element = XsdUtil.inutNfe.createTRetInutNfe((TRetInutNFe) obj);
                break;

            case SITUACAO_NFE_RET:
                context = JAXBContext.newInstance(TRetConsSitNFe.class);
                element = new br.com.swconsultoria.nfe.schema_4.retConsSitNFe.ObjectFactory().createRetConsSitNFe((TRetConsSitNFe) obj);
                break;

            case RET_RECIBO_NFE:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TRetConsReciNFe.class);
                    element = XsdUtil.NfeRt.createTRetConsReciNFe((br.com.swconsultoria.nfe.schema_rt.nfe.TRetConsReciNFe) obj);
                } else {
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe.class);
                    element = new br.com.swconsultoria.nfe.schema_4.retConsReciNFe.ObjectFactory().createRetConsReciNFe((br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TRetConsReciNFe) obj);
                }
                break;

            case RET_STATUS_SERVICO:
                context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ.class);
                element = new br.com.swconsultoria.nfe.schema_4.retConsStatServ.ObjectFactory().createRetConsStatServ((br.com.swconsultoria.nfe.schema_4.retConsStatServ.TRetConsStatServ) obj);
                break;

            case RET_CONS_CAD:
                context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad.class);
                element = new br.com.swconsultoria.nfe.schema.retConsCad.ObjectFactory().createRetConsCad((br.com.swconsultoria.nfe.schema.retConsCad.TRetConsCad) obj);
                break;

            case RET_DIST_DFE:
                context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt.class);
                element = XsdUtil.distDFeInt.createRetDistDFeInt((br.com.swconsultoria.nfe.schema.retdistdfeint.RetDistDFeInt) obj);
                break;

            case TPROCEVENTO:
                switch (obj.getClass().getName()) {
                    case TPROCCANCELAR:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancNFe.TProcEvento.class);
                        element = XsdUtil.envEventoCancNFe.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoCancNFe.TProcEvento) obj);
                        break;
                    case TPROCATORINTERESSADO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TProcEvento.class);
                        element = XsdUtil.envEventoAtorInteressado.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TProcEvento) obj);
                        break;
                    case TPROCCANCELARSUBST:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancSubst.TProcEvento.class);
                        element = XsdUtil.envEventoCancSubst.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoCancSubst.TProcEvento) obj);
                        break;
                    case TPROCCCE:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envcce.TProcEvento.class);
                        element = XsdUtil.envcce.createTProcEvento((br.com.swconsultoria.nfe.schema.envcce.TProcEvento) obj);
                        break;
                    case TPROCEPEC:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEpec.TProcEvento.class);
                        element = XsdUtil.epec.createTProcEvento((br.com.swconsultoria.nfe.schema.envEpec.TProcEvento) obj);
                        break;
                    case TPROCMAN:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envConfRecebto.TProcEvento.class);
                        element = XsdUtil.manifestacao.createTProcEvento((br.com.swconsultoria.nfe.schema.envConfRecebto.TProcEvento) obj);
                        break;
                    case TPROCINSUCESSO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TProcEvento.class);
                        element = XsdUtil.insucesso.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TProcEvento) obj);
                        break;
                    case TPROCCANCINSUCESSO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TProcEvento.class);
                        element = XsdUtil.cancInsucesso.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TProcEvento) obj);
                        break;
                    case TPROCECONF:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoEConf.TProcEvento.class);
                        element = XsdUtil.econf.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoEConf.TProcEvento) obj);
                        break;
                    case TPROCCANCECONF:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancEConf.TProcEvento.class);
                        element = XsdUtil.cancEConf.createTProcEvento((br.com.swconsultoria.nfe.schema.envEventoCancEConf.TProcEvento) obj);
                        break;
                    default:
                        throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                }

                break;

            case NFEPROC:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TNfeProc.class);
                    element = XsdUtil.NfeRt.createTNfeProc((br.com.swconsultoria.nfe.schema_rt.nfe.TNfeProc) obj);
                } else {
                    context = JAXBContext.newInstance(TNfeProc.class);
                    element = XsdUtil.enviNfe.createTNfeProc((TNfeProc) obj);
                }
                break;

            case NFE:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TNFe.class);
                    element = XsdUtil.NfeRt.createTNFe((br.com.swconsultoria.nfe.schema_rt.nfe.TNFe) obj);
                } else {
                    context = JAXBContext.newInstance(TNFe.class);
                    element = new JAXBElement<>(new QName("http://www.portalfiscal.inf.br/nfe", "NFe"), TNFe.class, null, (br.com.swconsultoria.nfe.schema_4.enviNFe.TNFe) obj);
                }
                break;

            case TPROCINUT:
                context = JAXBContext.newInstance(TProcInutNFe.class);
                element = XsdUtil.inutNfe.createTProcInutNFe((TProcInutNFe) obj);
                break;

            case EVENTO:
                switch (obj.getClass().getName()) {
                    case CANCELAR:
                        context = JAXBContext.newInstance(TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoCancNFe.ObjectFactory().createEnvEvento((TEnvEvento) obj);
                        break;
                    case CANCELAR_SUBSTITUICAO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancSubst.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoCancSubst.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancSubst.TEnvEvento) obj);
                        break;
                    case CCE:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envcce.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envcce.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envcce.TEnvEvento) obj);
                        break;
                    case EPEC:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEpec.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEpec.TEnvEvento) obj);
                        break;
                    case MANIFESTAR:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envConfRecebto.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envConfRecebto.TEnvEvento) obj);
                        break;
                    case ATOR_INTERESSADO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TEnvEvento) obj);
                        break;
                    case INSUCESSO_ENTREGA:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TEnvEvento) obj);
                        break;
                    case CANC_INSUCESSO_ENTREGA:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TEnvEvento) obj);
                        break;
                    case ECONF:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoEConf.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoEConf.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoEConf.TEnvEvento) obj);
                        break;
                    case CANC_ECONF:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancEConf.TEnvEvento.class);
                        element = new br.com.swconsultoria.nfe.schema.envEventoCancEConf.ObjectFactory().createEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancEConf.TEnvEvento) obj);
                        break;
                    default:
                        throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                }
                break;

            case RET_ENV_EVENTO:
                switch (obj.getClass().getName()) {
                    case RET_CANCELAR:
                        context = JAXBContext.newInstance(TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((TRetEnvEvento) obj);
                        break;
                    case RET_CANCELAR_SUBSTITUICAO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancSubst.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancSubst.TRetEnvEvento) obj);
                        break;
                    case RET_CCE:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envcce.TRetEnvEvento) obj);
                        break;
                    case RET_EPEC:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEpec.TRetEnvEvento) obj);
                        break;
                    case RET_MANIFESTAR:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envConfRecebto.TRetEnvEvento) obj);
                        break;
                    case RET_ATOR_INTERESSADO:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoAtorInteressado.TRetEnvEvento) obj);
                        break;
                    case RET_INSUCESSO_ENTREGA:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoInsucessoNFe.TRetEnvEvento) obj);
                        break;
                    case RET_CANC_INSUCESSO_ENTREGA:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancInsucessoNFe.TRetEnvEvento) obj);
                        break;
                    case RET_ECONF:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoEConf.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoEConf.TRetEnvEvento) obj);
                        break;
                    case RET_CANC_ECONF:
                        context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema.envEventoCancEConf.TRetEnvEvento.class);
                        element = XsdUtil.retEnvEvento.createTRetEnvEvento((br.com.swconsultoria.nfe.schema.envEventoCancEConf.TRetEnvEvento) obj);
                        break;
                    default:
                        throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                }
                break;

            case TProtNFe:
                if (layoutReforma) {
                    //TODO REMOVER DEPOIS DO LAYOUT REFORMA ENTRAR EM PRODUCAO
                    context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_rt.nfe.TProtNFe.class);
                    element = XsdUtil.NfeRt.createTProtNFe((br.com.swconsultoria.nfe.schema_rt.nfe.TProtNFe) obj);
                }else{
                    switch (obj.getClass().getName()) {
                        case TProtEnvi:
                            context = JAXBContext.newInstance(TProtNFe.class);
                            element = XsdUtil.enviNfe.createTProtNFe((br.com.swconsultoria.nfe.schema_4.enviNFe.TProtNFe) obj);
                            break;
                        case TProtCons:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe.class);
                            element = XsdUtil.retConsSitNfe.createTProtNFe((br.com.swconsultoria.nfe.schema_4.retConsSitNFe.TProtNFe) obj);
                            break;
                        case TProtReci:
                            context = JAXBContext.newInstance(br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe.class);
                            element = XsdUtil.retConsReciNfe.createTProtNFe((br.com.swconsultoria.nfe.schema_4.retConsReciNFe.TProtNFe) obj);
                            break;
                        default:
                            throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
                    }
                }
                break;

            default:
                throw new NfeException("Objeto não mapeado no XmlUtil:" + obj.getClass().getSimpleName());
        }
        assert context != null;
        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty("jaxb.encoding", "Unicode");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        StringWriter sw = new StringWriter(4096);

        String encodeXml = encode == null || !Charset.isSupported(encode.displayName()) ? "UTF-8" : encode.displayName();

        sw.append("<?xml version=\"1.0\" encoding=\"").append(encodeXml).append("\"?>");

        marshaller.marshal(element, sw);

        if ((obj.getClass().getSimpleName().equals(TPROCEVENTO))) {
            return replacesNfe(sw.toString().replace("procEvento", "procEventoNFe"));
        }

        return replacesNfe(sw.toString());

    }

    public static String gZipToXml(byte[] conteudo) throws IOException {
        if (conteudo == null || conteudo.length == 0) {
            return "";
        }
        GZIPInputStream gis;
        gis = new GZIPInputStream(new ByteArrayInputStream(conteudo));
        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8));
        StringBuilder outStr = new StringBuilder();
        String line;
        while ((line = bf.readLine()) != null) {
            outStr.append(line);
        }

        return outStr.toString();
    }

    public static String criaNfeProc(TEnviNFe enviNfe, Object retorno) throws JAXBException, NfeException {

        TNfeProc nfeProc = new TNfeProc();
        nfeProc.setVersao("4.00");
        nfeProc.setNFe(enviNfe.getNFe().get(0));
        String xml = XmlNfeUtil.objectToXml(retorno);
        nfeProc.setProtNFe(XmlNfeUtil.xmlToObject(xml, TProtNFe.class));

        return XmlNfeUtil.objectToXml(nfeProc);
    }

    private static String replacesNfe(String xml) {

        return xml.replace("<!\\[CDATA\\[<!\\[CDATA\\[", "<!\\[CDATA\\[")
                .replace("\\]\\]>\\]\\]>", "\\]\\]>")
                .replace("ns2:", "")
                .replace("ns3:", "")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("<Signature>", "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">")
                .replace(" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\"", "")
                .replace(" xmlns=\"\" xmlns:ns3=\"http://www.portalfiscal.inf.br/nfe\"", "")
                .replace("<NFe>", "<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\">");

    }

    /**
     * Le o Arquivo XML e retona String
     *
     * @return String
     * @throws NfeException
     */
    public static String leXml(String arquivo) throws IOException {

        ObjetoUtil.verifica(arquivo).orElseThrow(() -> new IllegalArgumentException("Arquivo xml não pode ser nulo/vazio."));
        if (!Files.exists(Paths.get(arquivo))) {
            throw new FileNotFoundException("Arquivo " + arquivo + " não encontrado.");
        }
        List<String> list = Files.readAllLines(Paths.get(arquivo));
        StringJoiner joiner = new StringJoiner("\n");
        list.forEach(joiner::add);

        return joiner.toString();
    }

    public static String dataNfe(LocalDateTime dataASerFormatada) {
        return dataNfe(dataASerFormatada, ZoneId.systemDefault());
    }

    public static String dataNfe(LocalDateTime dataASerFormatada, ZoneId zoneId) {
        try {
            GregorianCalendar calendar = GregorianCalendar.from(dataASerFormatada.atZone(ObjetoUtil.verifica(zoneId).orElse(ZoneId.of("Brazil/East"))));

            XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
            xmlCalendar.setMillisecond(DatatypeConstants.FIELD_UNDEFINED);
            return xmlCalendar.toString();

        } catch (DatatypeConfigurationException e) {
            log.warning(e.getMessage());
        }
        return null;
    }

    public static String getTag(String xml, String tag) throws NfeException {
        if (xml == null || xml.isEmpty()) {
            throw new NfeException("XML de entrada está vazio.");
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();
            Node node = (Node) xPath.evaluate("//*[local-name()='" + tag + "']", doc, XPathConstants.NODE);

            if (node == null) {
                throw new NfeException("Tag '" + tag + "' não encontrada no XML.");
            }

            return nodeToString(node);

        } catch (Exception e) {
            throw new NfeException("Erro ao extrair a tag '" + tag + "' do XML.\nErro: " + e.getMessage(), e);
        }
    }


    private static String nodeToString(Node node) {
        Document document = node.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = domImplLS.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        return serializer.writeToString(node);
    }
}
