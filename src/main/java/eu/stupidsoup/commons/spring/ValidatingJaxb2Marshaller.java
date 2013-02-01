package eu.stupidsoup.commons.spring;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

public class ValidatingJaxb2Marshaller extends Jaxb2Marshaller {
  private Schema schema;

  protected void initJaxbMarshaller(Marshaller marshaller) throws JAXBException {
    super.initJaxbMarshaller(marshaller);
    marshaller.setSchema(getGeneratedSchema());
  }

  protected void initJaxbUnmarshaller(Unmarshaller unmarshaller) throws JAXBException {
    super.initJaxbUnmarshaller(unmarshaller);
    unmarshaller.setSchema(getGeneratedSchema());
  }

  private Schema getGeneratedSchema() throws JAXBException {
    if (schema == null) {
      final List<StringWriterLSInput> schemaInputs = new ArrayList<StringWriterLSInput>();
      createSchemaInputsFromContext(schemaInputs);
      this.schema = generateSchemaFromInput(schemaInputs);
    }
    return schema;
  }

  private void createSchemaInputsFromContext(final List<StringWriterLSInput> serializedSchemas) throws JAXBException {
    JAXBContext context = this.getJaxbContext();
    try {
      context.generateSchema(new SchemaOutputResolver() {
        @Override
        public Result createOutput(String ns, String file) throws IOException {
          StringWriter writer = new StringWriter();
          serializedSchemas.add(new StringWriterLSInput(file, writer));
          StreamResult result = new StreamResult(writer);
          result.setSystemId(file);
          return result;
        }
      });
    } catch (IOException e) {
      throw new JAXBException(e);
    }
  }

  private Schema generateSchemaFromInput(final List<StringWriterLSInput> serializedSchemas) throws JAXBException {
    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    schemaFactory.setResourceResolver(new LSResourceResolver() {
      @Override
      public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        for (StringWriterLSInput input : serializedSchemas) {
          if (systemId != null && systemId.equals(input.getSystemId())) {
            input.setPublicId(publicId);
            return input;
          }
        }
        return null;
      }
    });

    try {
      List<StreamSource> schemaSources = new ArrayList<StreamSource>();
      for (StringWriterLSInput input : serializedSchemas) {
        schemaSources.add(new StreamSource(new StringReader(input.getStringData()), input.getSystemId()));
      }
      return schemaFactory.newSchema(schemaSources.toArray(new StreamSource[0]));
    } catch (SAXException e) {
      throw new JAXBException(e);
    }
  }

  public static class StringWriterLSInput implements LSInput {
    private final String systemId;
    private final StringWriter writer;
    private String publicId;

    public StringWriterLSInput(String systemId, StringWriter writer) {
      this.systemId = systemId;
      this.writer = writer;
    }

    @Override
    public Reader getCharacterStream() {
      return new StringReader(writer.toString());
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
    }

    @Override
    public InputStream getByteStream() {
      try {
        return new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void setByteStream(InputStream byteStream) {
    }

    @Override
    public String getStringData() {
      return writer.toString();
    }

    @Override
    public void setStringData(String stringData) {
    }

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
    public void setSystemId(String systemId) {
    }

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public String getBaseURI() {
      return null;
    }

    @Override
    public void setBaseURI(String baseURI) {
    }

    @Override
    public String getEncoding() {
      return null;
    }

    @Override
    public void setEncoding(String encoding) {
    }

    @Override
    public boolean getCertifiedText() {
      return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
    }

  }
}
