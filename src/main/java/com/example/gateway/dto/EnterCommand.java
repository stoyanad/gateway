package com.example.gateway.dto;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Setter;

@Setter
public class EnterCommand {

    private String session;
    private Long timestamp;
    private String player;

    @XmlAttribute
    public String getSession() {
        return session;
    }

    @XmlElement
    public Long getTimestamp() {
        return timestamp;
    }

    @XmlElement
    public String getPlayer() {
        return player;
    }

}
