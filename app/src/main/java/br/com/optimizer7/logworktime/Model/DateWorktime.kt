package br.com.optimizer7.logworktime.Model

import com.google.api.client.util.DateTime

data class DateWorktime constructor( val dateWorktime: DateTime? = null,
                                     val worktime: Worktime? = null )