package br.com.optimizer7.logworktime.Model

import com.google.api.client.util.DateTime

data class DateWorktime constructor( val dateWorktime: String? = null,
                                     val worktime: Worktime? = null )