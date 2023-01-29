interface AtmAvailable {
    EnumMap<Nominal, Long> available(Multivalute valute)
}

interface AtmMachine extends AtmAvailable {

    EnumMap<Nominal, Long> withdraw(Multivalute valute, long nonNegativeSum)

    long deposit(Multivalute valute, EnumMap<Nominal, Long> packet)

    long balanceTotal(Multivalute valute)

}

interface AtmCartridge {

    void charging(Multivalute valute, Nominal nominal, long amount)

    void discharging(Multivalute valute, Nominal nominal, long amount)

    void chargingByCartridge(Cartridge cartridge)

    void dischargingByCartridge(Cartridge cartridge)

    Nominal getMinimalAvailableNominal()

}


interface WithdrawStrategy {

    /**
     *
     * @param cartridge -  ATM cartridge
     * @param nonNegativeSum - amount for withdraw
     * @return - withdrawed nominals packet
     */
    EnumMap<Nominal, Long> tryWithdraw(Cartridge cartridge, long nonNegativeSum)

}
