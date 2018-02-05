package br.com.optimizer7.logworktime.Model

data class Worktime constructor( val beginWorktime: String? = null,
                                 val beginLunch: String? = null,
                                 val doneLunch: String? = null,
                                 val doneWorktime: String? = null)