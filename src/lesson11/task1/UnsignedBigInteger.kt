@file:Suppress("UNUSED_PARAMETER")

package lesson11.task1

import java.lang.StringBuilder

fun Int.toUnsignedBigInteger(): UnsignedBigInteger = UnsignedBigInteger(this)

fun String.toUnsignedBigInteger(): UnsignedBigInteger = UnsignedBigInteger(this)

/**
 * Класс "беззнаковое большое целое число".
 *
 * Общая сложность задания -- очень сложная, общая ценность в баллах -- 32.
 * Объект класса содержит целое число без знака произвольного размера
 * и поддерживает основные операции над такими числами, а именно:
 * сложение, вычитание (при вычитании большего числа из меньшего бросается исключение),
 * умножение, деление, остаток от деления,
 * преобразование в строку/из строки, преобразование в целое/из целого,
 * сравнение на равенство и неравенство
 */
public class UnsignedBigInteger : Comparable<UnsignedBigInteger> {
    private val _numbers: ByteArray

    /**
     * Конструктор из строки
     */
    public constructor(string: String) {
        _numbers = parseString(string)

        assertNumbers(_numbers)
    }

    /**
     * Конструктор из целого
     */
    public constructor(number: Int) {
        _numbers = parseString(number.toString())

        assertNumbers(_numbers)
    }

    private constructor(numbers: ByteArray) {
        _numbers = numbers

        assertNumbers(_numbers)
    }

    /**
     * Сложение
     */
    public operator fun plus(other: UnsignedBigInteger): UnsignedBigInteger {
        val currentNumbers = if (_numbers.count() > other._numbers.count()) _numbers else other._numbers
        val otherNumbers = if (_numbers.count() <= other._numbers.count()) _numbers else other._numbers

        val currentNumbersCount = currentNumbers.count()
        val otherNumbersCount = otherNumbers.count()
        val diffNumbersCount = currentNumbersCount - otherNumbersCount

        val output = mutableListOf<Byte>()

        var deposit = false

        for (index in currentNumbersCount - 1 downTo 0) {
            val currentNumber = currentNumbers[index]

            var number: Int = currentNumber.toInt()

            val otherNumIndex = index - diffNumbersCount
            if (otherNumIndex >= 0) {
                number += otherNumbers[otherNumIndex]
            }

            if (deposit) {
                number += 1
                deposit = false
            }

            if (number >= 10) {
                number -= 10
                deposit = true
            }

            output.add(0, number.toByte())
        }

        if (deposit) {
            output.add(0, 1.toByte())
        }

        return UnsignedBigInteger(output.toByteArray())
    }

    /**
     * Вычитание (бросить ArithmeticException, если this < other)
     */
    public operator fun minus(other: UnsignedBigInteger): UnsignedBigInteger {
        if (this < other) throw ArithmeticException()
        if (this == other) return 0.toUnsignedBigInteger()

        val currentNumbers = _numbers;
        val otherNumbers = other._numbers;

        val currentNumbersCount = currentNumbers.count()
        val otherNumbersCount = otherNumbers.count()
        val diffNumbersCount = currentNumbersCount - otherNumbersCount

        val output = mutableListOf<Byte>()

        var credit = false

        for (index in currentNumbersCount - 1 downTo 0) {
            val currentNumber = currentNumbers[index]

            var number: Int = currentNumber.toInt()

            val otherNumIndex = index - diffNumbersCount
            if (otherNumIndex >= 0) {
                number -= otherNumbers[otherNumIndex]
            }

            if (credit) number -= 1

            if (number < 0) {
                number += 10
                credit = true
            }

            if (credit && currentNumber != 0.toByte()) credit = false

            output.add(0, number.toByte())
        }

        removeWhileZero(output)

        return UnsignedBigInteger(output.toByteArray())
    }

    /**
     * Умножение
     */
    public operator fun times(other: UnsignedBigInteger): UnsignedBigInteger {
        val zero = 0.toUnsignedBigInteger()
        val one = 1.toUnsignedBigInteger()

        val multiplier = if (other > this) this else other
        val multiplable = if (other <= this) this else other

        var index = multiplier
        var value = zero

        while (index != zero) {
            index = index.minus(one);

            value = value.plus(multiplable)
        }

        return value
    }

    /**
     * Деление
     */
    public operator fun div(other: UnsignedBigInteger): UnsignedBigInteger {
        val zero = 0.toUnsignedBigInteger()
        val one = 1.toUnsignedBigInteger()

        val divider = other
        val dividable = this

        var count = zero
        var value = zero

        do {
            count = count.plus(one);

            value = value.plus(divider)
        } while (value < dividable)

        return count
    }

    /**
     * Взятие остатка
     */
    public operator fun rem(other: UnsignedBigInteger): UnsignedBigInteger = TODO()

    /**
     * Сравнение на равенство (по контракту Any.equals)
     */
    public override fun equals(other: Any?): Boolean {
        if (other !is UnsignedBigInteger) return false

        return _numbers.contentEquals(other._numbers)
    }

    /**
     * Сравнение на больше/меньше (по контракту Comparable.compareTo)
     */
    public override fun compareTo(other: UnsignedBigInteger): Int {
        val currentNumbersCount = _numbers.count()
        val otherNumbersCount = other._numbers.count()

        if (currentNumbersCount != otherNumbersCount) {
            return if (currentNumbersCount > otherNumbersCount) 1 else -1
        }

        for (index in 0 until currentNumbersCount) {
            val currentNumber = _numbers[index]
            val otherNumber = other._numbers[index]

            if (currentNumber != otherNumber) {
                return if (currentNumber > otherNumber) 1 else -1
            }
        }

        return 0
    }

    /**
     * Преобразование в строку
     */
    public override fun toString(): String {
        val builder = StringBuilder()

        for (number in _numbers) builder.append(number)

        return builder.toString()
    }

    /**
     * Преобразование в целое
     * Если число не влезает в диапазон Int, бросить ArithmeticException
     */
    public fun toInt(): Int {
        if (_numbers.count() > Int.MAX_VALUE) throw ArithmeticException()

        var int = 0
        for ((index, number) in _numbers.withIndex()) {
            int += number

            if (index != _numbers.count() - 1) int *= 10
        }

        return int
    }

    private fun assertNumbers(numbers: ByteArray) {
        assert(numbers.isNotEmpty())

        if (numbers.count() > 1) assert(numbers.first() != 0.toByte())

        for (number in numbers) assertNumber(number)
    }

    private fun assertNumber(number: Byte) {
        assert(number >= 0)
        assert(number <= 9)
    }

    private fun parseString(string: String): ByteArray {
        return string.map { it.toString().toInt().toByte() }.toByteArray()
    }

    private fun removeWhileZero(list: MutableList<Byte>) {
        for (item in list.toList()) {
            if (item == 0.toByte()) list.removeAt(0)
            else return;
        }
    }
}