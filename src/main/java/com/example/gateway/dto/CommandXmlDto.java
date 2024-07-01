package com.example.gateway.dto;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Setter;

@Setter
@XmlRootElement(name = "command")
public class CommandXmlDto {

    private String id;
    private EnterCommand enter;
    private GetCommand get;

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    @XmlElement(name = "enter")
    public EnterCommand getEnter() {
        return enter;
    }

    @XmlElement(name = "get")
    public GetCommand getGet() {
        return get;
    }

}
