package bank.app.springbootbankapp.mapper;

import bank.app.springbootbankapp.dto.TransactionDto;
import bank.app.springbootbankapp.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "accountFrom.number", target = "accountFromNumber")
    @Mapping(source = "accountTo.number", target = "accountToNumber")
    TransactionDto toTransactionDto(Transaction transaction);
}
