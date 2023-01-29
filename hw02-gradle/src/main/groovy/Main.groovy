import static Multivalute.RUR

static void main(String[] args) {

    def c = RUR

    Cartridge crt = new Cartridge()
    crt.charging(c, Nominal.N10, 1000)
    crt.charging(c, Nominal.N10, 101)
    crt.charging(c, Nominal.N100, 100)
    crt.charging(c, Nominal.N1000, 100)
    crt.charging(c, Nominal.N200, 100)
    crt.charging(c, Nominal.N2000, 0) // empty slot
    crt.charging(c, Nominal.N50, 10)
    crt.charging(c, Nominal.N50, 101)
    crt.charging(c, Nominal.N500, 100)
    crt.charging(c, Nominal.N5000, 10)

    crt.discharging(c, Nominal.N200, 99)
    crt.discharging(c, Nominal.N500, 9)
//    crt.discharging(c, Nominal.N2000, 1) // Impossible Discharging, insufficient money. Expression: (newAmount >= 0). Values: newAmount = -1

    println "Initial Cartridge capacity: ${crt.available(RUR).toString()}"

    EnumMap p1 = new EnumMap(Nominal)
    p1.put(Nominal.N10, 10000)
    WithdrawStrategy stg = new TrivialWithdrawStrategyImpl()
    def atm = new Atm(cartridge: crt, strategy: stg)
    println "Current balance ${atm.balanceTotal(c)} ${c}"
    println "Put money to atm ${atm.deposit(c, p1)} ${c}"
    println "Current balance ${atm.balanceTotal(c)} ${c}"
//    println atm.withdraw(c, 5) //Impossible Withdraw. SumForWithdraw < Minimal available banknote.
    def nns = 77520
    println "Get $nns money from atm ${atm.withdraw(c, nns)}" // [N5000:10, N1000:27, N500:1, N10:2]
//    println atm.withdraw(c,77521) // Impossible Withdraw. Not a round number or small sum
    println "Current balance ${atm.balanceTotal(c)} ${c}"

}


