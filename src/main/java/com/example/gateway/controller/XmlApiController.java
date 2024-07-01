package com.example.gateway.controller;

import com.example.gateway.dto.CommandXmlDto;
import com.example.gateway.model.FindGetCommand;
import com.example.gateway.model.InsertEnterCommand;
import com.example.gateway.service.CommandConversionService;
import com.example.gateway.service.GatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.List;

@RestController
@RequestMapping("/xml_api")
@Tag(name = "XML API", description = "API for XML-based commands")
public class XmlApiController {

    private final GatewayService gatewayService;
    private final CommandConversionService commandConversionService;

    @Autowired
    public XmlApiController(GatewayService gatewayService, CommandConversionService commandConversionService) {
        this.gatewayService = gatewayService;
        this.commandConversionService = commandConversionService;
    }

    @PostMapping("/command")
    @Operation(summary = "Process XML command", description = "Process an XML command which can be either an enter or a get command.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed command"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate requestId", content = @Content),
            @ApiResponse(responseCode = "500", description = "Failed to process request", content = @Content)
    })
    public ResponseEntity<?> processXmlCommand(@RequestBody String xmlRequest) {
        try {
            CommandXmlDto commandXmlDto = parseRequest(xmlRequest);
            if (commandXmlDto.getEnter() != null) {
                InsertEnterCommand command = commandConversionService.convertEnter(commandXmlDto);
                gatewayService.processInsertEnterCommand(command);
                return ResponseEntity.ok().build();
            } else if (commandXmlDto.getGet() != null) {
                FindGetCommand command = commandConversionService.convertGet(commandXmlDto);
                List<String> requestIds = gatewayService.processFindGetCommand(command);
                return ResponseEntity.ok(requestIds);
            } else {
                return ResponseEntity.badRequest().body("Invalid command type");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Duplicate requestId");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to process request: " + e.getMessage());
        }
    }

    @Operation(hidden = true)
    public CommandXmlDto parseRequest(String xmlRequest) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(CommandXmlDto.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (CommandXmlDto) unmarshaller.unmarshal(new StringReader(xmlRequest));
    }
}
