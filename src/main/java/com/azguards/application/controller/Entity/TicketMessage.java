package com.azguards.application.controller.Entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketMessage {
    private String id;
    private String name;
    private String email;
    private String content;
}
