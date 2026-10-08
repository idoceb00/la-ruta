package com.idoceb00.laruta.backend.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InviteCodeGeneratorTest {

    // 8 characters, uppercase letters and digits without the ambiguous I, O, 0 and 1
    private static final String INVITE_CODE_PATTERN = "^[A-HJ-NP-Z2-9]{8}$";
    private static final int SAMPLES = 1_000;

    private final InviteCodeGenerator generator = new InviteCodeGenerator();

    @Test
    void generate_returnsEightCharacterCode() {
        assertThat(generator.generate()).hasSize(8);
    }

    @Test
    void generate_usesOnlyUnambiguousUppercaseLettersAndDigits() {
        for (int i = 0; i < SAMPLES; i++) {
            assertThat(generator.generate()).matches(INVITE_CODE_PATTERN);
        }
    }

    // 32^8 possible codes: a collision in 1000 samples would point to a broken random source
    @Test
    void generate_whenCalledRepeatedly_returnsDifferentCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < SAMPLES; i++) {
            codes.add(generator.generate());
        }

        assertThat(codes).hasSize(SAMPLES);
    }
}