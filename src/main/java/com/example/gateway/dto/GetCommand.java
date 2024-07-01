package com.example.gateway.dto;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Setter;

@Setter
public class GetCommand {

    private String session;

    @XmlAttribute
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
