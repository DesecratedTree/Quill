package dawn.cs2.unscramble

object UnscrambleUtils {

    @JvmStatic
    fun read(version: Int, scramble: HashMap<Int, Int>, unscramble: HashMap<Int, Int>) {
        val text = javaClass.getResource("/cs2/opcode/unscramble/$version.txt").readText()
        for (line in text.lines()) {
            val opcodes = line.split(" ")
            if (opcodes.size < 2 || opcodes[0] == "?" || opcodes[0].startsWith("#")) {
                continue
            }
            val master = opcodes[0].toInt()
            val scrambledValue = opcodes[1].toInt()
            scramble[master] = scrambledValue
            unscramble[scrambledValue] = master
        }
    }
}
