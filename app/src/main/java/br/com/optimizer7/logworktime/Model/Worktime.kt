package br.com.optimizer7.logworktime.Model

import com.google.api.client.util.DateTime

data class Worktime constructor( val beginWorktime: String? = null,
                                 val beginLunch: String? = null,
                                 val doneLunch: String? = null,
                                 val doneWorktime: String? = null)