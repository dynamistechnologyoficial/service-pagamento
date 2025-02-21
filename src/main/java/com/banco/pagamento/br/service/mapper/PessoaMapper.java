package com.banco.pagamento.br.service.mapper;

import com.banco.pagamento.br.domain.Pessoa;
import com.banco.pagamento.br.service.dto.PessoaDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Pessoa} and its DTO {@link PessoaDTO}.
 */
@Mapper(componentModel = "spring")
public interface PessoaMapper extends EntityMapper<PessoaDTO, Pessoa> {}
