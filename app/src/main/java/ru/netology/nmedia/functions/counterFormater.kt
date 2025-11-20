package ru.netology.nmedia.functions

fun counterFormatter(count: Int) : String{
    return when {
        count < 1000 -> count.toString()
        count < 1100 -> "1K"
        count < 10_000 ->{
            val thousands = count / 1000
            val hundreds = (count % 1000) / 100
            "${thousands}.${hundreds}K"
        }
        count < 1_000_000 -> {
            "${count / 1000}K"
        }
        count < 1_100_000 -> "1M"
        else -> {
            val millions = count / 1_000_000
            val hundredThousands = (count % 1_000_000) / 100_000
            "${millions}.${hundredThousands}M"
        }
    }
}