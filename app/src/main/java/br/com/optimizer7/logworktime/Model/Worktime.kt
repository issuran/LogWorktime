package br.com.optimizer7.logworktime.Model

import java.util.*

data class Worktime constructor( val beginWorktime: String? = null,
                                 val beginLunch: String? = null,
                                 val doneLunch: String? = null,
                                 val doneWorktime: String? = null,
                                 val date: String? = null,
                                 val id: Int? = null)