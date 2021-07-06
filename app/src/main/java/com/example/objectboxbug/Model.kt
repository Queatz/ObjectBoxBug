package com.example.objectboxbug

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class Model {
    @Id var objectBoxId: Long = 0
//    @Unique(onConflict = ConflictStrategy.REPLACE) var id: String? = null
    var name: String? = null
}