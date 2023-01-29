import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class AtmToolTest {

    EnumMap<Nominal, Long> p1 = new EnumMap<>(Nominal.class);

    @BeforeEach
    void setUp() {
        p1.clear();
        p1.put(Nominal.N10, 10000L);
    }

    @Test
    void summarize() {
        assertEquals(AtmTool.summarize(p1), 100000L);
    }

    @Test
    void testSummarize() {
        Cartridge crt = new Cartridge();
        assertEquals(AtmTool.summarize(p1, Multivalute.RUR, crt), 100000L);
        assertEquals(crt.available(Multivalute.RUR).get(Nominal.N10),10000L);
    }
}

