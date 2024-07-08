package com.example.gateway.service;

import com.example.gateway.dto.CommandXmlDto;
import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import org.springframework.stereotype.Service;

@Service
public class CommandConversionService {

    public InsertEnterCommand convertEnter(CommandXmlDto xmlCommand) {
        InsertEnterCommand unifiedCommand = new InsertEnterCommand();
        unifiedCommand.setRequestId(xmlCommand.getId());
        unifiedCommand.setTimestamp(xmlCommand.getEnter().getTimestamp());
        unifiedCommand.setUserId(xmlCommand.getEnter().getPlayer());
        unifiedCommand.setSessionId(xmlCommand.getEnter().getSession());
        return unifiedCommand;
    }

    public FindGetCommand convertGet(CommandXmlDto xmlCommand) {
        FindGetCommand unifiedCommand = new FindGetCommand();
        unifiedCommand.setRequestId(xmlCommand.getId());
        unifiedCommand.setSessionId(xmlCommand.getGet().getSession());
        return unifiedCommand;
    }

}
