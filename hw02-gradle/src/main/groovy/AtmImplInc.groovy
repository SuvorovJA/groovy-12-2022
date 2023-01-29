/**
 * ATM of Impl Incorporated
 */

class Atm implements AtmMachine, AtmAvailable {

    private Cartridge cartridge
    private WithdrawStrategy strategy

    @Override
    EnumMap<Nominal, Long> withdraw(Multivalute valute, long nonNegativeSum) {
        assert nonNegativeSum >= 0: "Withdraw attempt with negative sum"
        strategy.tryWithdraw(cartridge, nonNegativeSum)
    }

    @Override
    long deposit(Multivalute valute, EnumMap<Nominal, Long> packet) {
        AtmTool.summarize(packet, valute, cartridge)
    }

    @Override
    long balanceTotal(Multivalute valute) {
        AtmTool.summarize(cartridge.available(valute))
    }

    @Override
    EnumMap<Nominal, Long> available(Multivalute valute) {
        cartridge.available(valute)
    }
}


class Cartridge implements AtmCartridge, AtmAvailable {

    private EnumMap<Nominal, Long> capacity = new EnumMap<>(Nominal.class)

    @Override
    EnumMap<Nominal, Long> available(Multivalute valute) {
        capacity.clone()
    }

    @Override
    void charging(Multivalute valute, Nominal nominal, long amount) {
        long oldAmount = 0
        if (capacity.containsKey(nominal)) oldAmount = capacity.get(nominal)
        capacity.put(nominal, oldAmount + amount)
    }

    @Override
    void discharging(Multivalute valute, Nominal nominal, long amount) {
        long oldAmount = 0
        if (capacity.containsKey(nominal)) oldAmount = capacity.get(nominal)
        def newAmount = oldAmount - amount
        assert newAmount >= 0: "Impossible Discharging, insufficient money"
        capacity.put(nominal, newAmount)
    }

    @Override
    void chargingByCartridge(Cartridge cartridge) {
        EnumMap<Nominal, Long> external = cartridge.available(Multivalute.RUR)
        external.keySet().each {
            if (external.get(it)) {
                long oldAmount = 0
                if (capacity.get(it)) oldAmount = capacity.get(it)
                capacity.put(it, oldAmount + external.get(it))
            }
        }
    }

    @Override
    void dischargingByCartridge(Cartridge cartridge) {
        EnumMap<Nominal, Long> external = cartridge.available(Multivalute.RUR)
        external.keySet().each {
            if (external.get(it)) {
                long oldAmount = 0
                if (capacity.get(it)) oldAmount = capacity.get(it)
                assert oldAmount >= external.get(it): 'Impossible Discharging. Old < Sub for nominal ${nominal}'
                capacity.put(it, oldAmount - external.get(it))
            }
        }
    }

    @Override
    Nominal getMinimalAvailableNominal() {
        Nominal.values()
                .findAll { it.getNominal() > 0 }
                .min({ it.getNominal() })
    }

}

class AtmTool {

    // а каррированием?
    static long summarize(EnumMap<Nominal, Long> packet) {
        summarize(packet, null, null)
    }

    static long summarize(EnumMap<Nominal, Long> packet, Multivalute valute, Cartridge cartridge) {
        long sum = 0
        packet.keySet().each {
            cartridge?.charging(valute, it, packet.get(it))
            sum += packet.get(it) * it.getNominal()
        }
        sum
    }

}


class TrivialWithdrawStrategyImpl implements WithdrawStrategy {

    @Override
    EnumMap<Nominal, Long> tryWithdraw(Cartridge cartridge, long nonNegativeSumForWithdraw) {

        Cartridge packetOut = new Cartridge()
        Cartridge packetIn = new Cartridge()
        packetIn.chargingByCartridge(cartridge)

        assert nonNegativeSumForWithdraw >= 0: "Negative Sum"
        assert AtmTool.summarize(cartridge.available(Multivalute.RUR)) >= nonNegativeSumForWithdraw: "Impossible Withdraw. SumForWithdraw > Cartridge capacity."
        assert packetIn.getMinimalAvailableNominal() != null: "Impossible Withdraw. Empty cartridge."
        def minimalAvailableNominalBanknote = packetIn.getMinimalAvailableNominal().getNominal()
        assert minimalAvailableNominalBanknote <= nonNegativeSumForWithdraw: "Impossible Withdraw. SumForWithdraw < Minimal available banknote."

        def left = nonNegativeSumForWithdraw
        while (left > 0) {
            for (Nominal nominal : packetIn.available(Multivalute.RUR).keySet()) {
                assert left >= minimalAvailableNominalBanknote: "Impossible Withdraw. Not a round number or small sum."
                if (left < nominal.getNominal()) continue
                if (packetIn.available(Multivalute.RUR).get(nominal) == null) continue
                if (packetIn.available(Multivalute.RUR).get(nominal) <= 0) continue
                left -= nominal.getNominal()
                packetIn.discharging(Multivalute.RUR, nominal, 1)
                packetOut.charging(Multivalute.RUR, nominal, 1)
                break
            }
        }

        assert left >= 0 || AtmTool.summarize(packetOut.available(Multivalute.RUR)) == nonNegativeSumForWithdraw: "Impossible Withdraw. Err in algorithm."

        cartridge.dischargingByCartridge(packetOut)

        packetOut.available(Multivalute.RUR)
    }

}
