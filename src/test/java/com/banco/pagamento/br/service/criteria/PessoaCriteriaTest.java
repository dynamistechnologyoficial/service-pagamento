package com.banco.pagamento.br.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class PessoaCriteriaTest {

    @Test
    void newPessoaCriteriaHasAllFiltersNullTest() {
        var pessoaCriteria = new PessoaCriteria();
        assertThat(pessoaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void pessoaCriteriaFluentMethodsCreatesFiltersTest() {
        var pessoaCriteria = new PessoaCriteria();

        setAllFilters(pessoaCriteria);

        assertThat(pessoaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void pessoaCriteriaCopyCreatesNullFilterTest() {
        var pessoaCriteria = new PessoaCriteria();
        var copy = pessoaCriteria.copy();

        assertThat(pessoaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(pessoaCriteria)
        );
    }

    @Test
    void pessoaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var pessoaCriteria = new PessoaCriteria();
        setAllFilters(pessoaCriteria);

        var copy = pessoaCriteria.copy();

        assertThat(pessoaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(pessoaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var pessoaCriteria = new PessoaCriteria();

        assertThat(pessoaCriteria).hasToString("PessoaCriteria{}");
    }

    private static void setAllFilters(PessoaCriteria pessoaCriteria) {
        pessoaCriteria.id();
        pessoaCriteria.nome();
        pessoaCriteria.dtNascimento();
        pessoaCriteria.cpf();
        pessoaCriteria.email();
        pessoaCriteria.createdBy();
        pessoaCriteria.createdDate();
        pessoaCriteria.lastModifiedBy();
        pessoaCriteria.lastModifiedDate();
        pessoaCriteria.distinct();
    }

    private static Condition<PessoaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getNome()) &&
                condition.apply(criteria.getDtNascimento()) &&
                condition.apply(criteria.getCpf()) &&
                condition.apply(criteria.getEmail()) &&
                condition.apply(criteria.getCreatedBy()) &&
                condition.apply(criteria.getCreatedDate()) &&
                condition.apply(criteria.getLastModifiedBy()) &&
                condition.apply(criteria.getLastModifiedDate()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<PessoaCriteria> copyFiltersAre(PessoaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getNome(), copy.getNome()) &&
                condition.apply(criteria.getDtNascimento(), copy.getDtNascimento()) &&
                condition.apply(criteria.getCpf(), copy.getCpf()) &&
                condition.apply(criteria.getEmail(), copy.getEmail()) &&
                condition.apply(criteria.getCreatedBy(), copy.getCreatedBy()) &&
                condition.apply(criteria.getCreatedDate(), copy.getCreatedDate()) &&
                condition.apply(criteria.getLastModifiedBy(), copy.getLastModifiedBy()) &&
                condition.apply(criteria.getLastModifiedDate(), copy.getLastModifiedDate()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
