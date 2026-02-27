package bank.app.springbootbankapp.mapper;

import bank.app.springbootbankapp.dto.TransferRequestDto;
import bank.app.springbootbankapp.dto.TransferResponseDto;
import bank.app.springbootbankapp.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferResponseDto  toResponseDto(Transaction transaction);

}
