package example.combiner;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;

@JacksonXmlRootElement(localName = "data")
public class XmlData {

    @JacksonXmlProperty
    public long timestamp;
    @JacksonXmlProperty
    public BigDecimal amount;

    public XmlData() {
    }

    public XmlData(long timestamp, BigDecimal amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "XmlData -> timestamp: " + this.timestamp + ", amount: " + this.amount.toPlainString();
    }

}
