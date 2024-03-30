package com.tamnaju.dev.domains.mappers;

import org.apache.ibatis.annotations.Mapper;

import com.tamnaju.dev.domains.entities.SignatureEntity;

@Mapper
public interface SignatureMapper {
    public SignatureEntity getSignatureEntity();
}
