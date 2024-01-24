package com.tamnaju.dev.domains.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureEntity {
    private byte[] keyByte;
    private LocalDateTime createdAt;
}
