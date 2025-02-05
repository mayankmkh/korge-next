package com.soywiz.kds

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun Random.intStream(): Sequence<Int> = sequence { while (true) yield(nextInt()) }
fun Random.intStream(from: Int, until: Int): Sequence<Int> = sequence { while (true) yield(nextInt(from, until)) }
fun Random.intStream(range: IntRange): Sequence<Int> = intStream(range.start, range.endInclusive + 1)

class IntMapTest {
    @Test
    fun smoke() {
        val values = Random(0).intStream().take(10000).toList().distinct()
        val valuesSet = values.toSet()
        val m = IntMap<String>()
        for (value in values) m[value] = "value"
        assertEquals(m.size, values.size)
        val notIn = (0 until 10001).asSequence().firstOrNull { it !in valuesSet } ?: error("Unexpected")
        assertEquals(false, m.contains(notIn))
        for (key in m.keys.toList()) {
            assertEquals(true, m.contains(key))
            assertEquals(true, key in m)
        }

        val removeValues = values.take(values.size / 2)
        for (key in removeValues) m.remove(key)
        assertEquals(values.size - removeValues.size, m.size)

        val containingValues = values.drop(removeValues.size)
        for (key in removeValues) assertEquals(false, key in m)
        for (key in containingValues) assertEquals(true, key in m)
    }

    @Test
    fun simple() {
        val m = IntMap<String>()
        assertEquals(0, m.size)
        assertEquals(null, m[0])

        m[0] = "test"
        assertEquals(1, m.size)
        assertEquals("test", m[0])
        assertEquals(null, m[1])

        m[0] = "test2"
        assertEquals(1, m.size)
        assertEquals("test2", m[0])
        assertEquals(null, m[1])

        m.remove(0)
        assertEquals(0, m.size)
        assertEquals(null, m[0])
        assertEquals(null, m[1])

        m.remove(0)
    }

    @Test
    fun name2() {
        val m = IntMap<Int>()
        for (n in 0 until 1000) m[n] = n * 1000
        for (n in 0 until 1000) assertEquals(n * 1000, m[n])
        assertEquals(null, m[-1])
        assertEquals(null, m[1001])
    }

    private val items by lazy { listOf(0, 13, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276, 277, 278, 279, 280, 281, 282, 283, 284, 285, 286, 287, 288, 289, 290, 291, 292, 293, 294, 295, 296, 297, 298, 299, 300, 301, 302, 303, 304, 305, 306, 307, 308, 309, 310, 311, 312, 313, 314, 315, 316, 317, 318, 319, 320, 321, 322, 323, 324, 325, 326, 327, 328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346, 347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365, 366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 884, 900, 901, 902, 903, 904, 905, 906, 908, 910, 911, 912, 913, 914, 915, 916, 917, 918, 919, 920, 921, 922, 923, 924, 925, 926, 927, 928, 929, 931, 932, 933, 934, 935, 936, 937, 938, 939, 940, 941, 942, 943, 944, 945, 946, 947, 948, 949, 950, 951, 952, 953, 954, 955, 956, 957, 958, 959, 960, 961, 962, 963, 964, 965, 966, 967, 968, 969, 970, 971, 972, 973, 974, 977, 978, 982, 1024, 1025, 1026, 1027, 1028, 1029, 1030, 1031, 1032, 1033, 1034, 1035, 1036, 1037, 1038, 1039, 1040, 1041, 1042, 1043, 1044, 1045, 1046, 1047, 1048, 1049, 1050, 1051, 1052, 1053, 1054, 1055, 1056, 1057, 1058, 1059, 1060, 1061, 1062, 1063, 1064, 1065, 1066, 1067, 1068, 1069, 1070, 1071, 1072, 1073, 1074, 1075, 1076, 1077, 1078, 1079, 1080, 1081, 1082, 1083, 1084, 1085, 1086, 1087, 1088, 1089, 1090, 1091, 1092, 1093, 1094, 1095, 1096, 1097, 1098, 1099, 1100, 1101, 1102, 1103, 1104, 1105, 1106, 1107, 1108, 1109, 1110, 1111, 1112, 1113, 1114, 1115, 1116, 1117, 1118, 1119, 1120, 1121, 1122, 1123, 1124, 1125, 1126, 1127, 1128, 1129, 1130, 1131, 1132, 1133, 1134, 1135, 1136, 1137, 1138, 1139, 1140, 1141, 1142, 1143, 1144, 1145, 1146, 1147, 1148, 1149, 1150, 1151, 1152, 1153, 1154, 1155, 1156, 1157, 1158, 1160, 1161, 1162, 1163, 1164, 1165, 1166, 1167, 1168, 1169, 1170, 1171, 1172, 1173, 1174, 1175, 1176, 1177, 1178, 1179, 1180, 1181, 1182, 1183, 1184, 1185, 1186, 1187, 1188, 1189, 1190, 1191, 1192, 1193, 1194, 1195, 1196, 1197, 1198, 1199, 1200, 1201, 1202, 1203, 1204, 1205, 1206, 1207, 1208, 1209, 1210, 1211, 1212, 1213, 1214, 1215, 1216, 1217, 1218, 1219, 1220, 1221, 1222, 1223, 1224, 1225, 1226, 1227, 1228, 1229, 1230, 1231, 1232, 1233, 1234, 1235, 1236, 1237, 1238, 1239, 1240, 1241, 1242, 1243, 1244, 1245, 1246, 1247, 1248, 1249, 1250, 1251, 1252, 1253, 1254, 1255, 1256, 1257, 1258, 1259, 1260, 1261, 1262, 1263, 1264, 1265, 1266, 1267, 1268, 1269, 1270, 1271, 1272, 1273, 1274, 1275, 1276, 1277, 1278, 1279, 1280, 1281, 1282, 1283, 1284, 1285, 1286, 1287, 1288, 1289, 1290, 1291, 1292, 1293, 1294, 1295, 1296, 1297, 1298, 1299, 8192, 8193, 8194, 8195, 8196, 8197, 8198, 8199, 8200, 8201, 8202, 8203, 8211, 8212, 8213, 8215, 8216, 8217, 8218, 8219, 8220, 8221, 8222, 8224, 8225, 8226, 8230, 8240, 8242, 8243, 8249, 8250, 8252, 8260, 8355, 8356, 8358, 8359, 8360, 8361, 8363, 8364, 8369, 8377, 8378) }

    @Test
    fun testLimits() {
        val set = LinkedHashSet<Int>()
        val map = IntMap<Unit>()
        val mapInt = IntIntMap()
        for (i in items) {
            for (j in items) {
                if (j % 4 == 0) {
                    val key = i or (j shl 16)
                    set += key
                    map[key] = Unit
                    mapInt[key] = 1
                }
            }
        }
        assertEquals(131211, map.size)
        assertEquals(131211, mapInt.size)
        assertTrue { map.backSize <= 4194789 }
        assertTrue { mapInt.backSize <= 4194789 }
        assertTrue { map.stashSize <= 512 }
        assertTrue { mapInt.stashSize <= 512 }
        assertTrue { set.all { it in map } }
        assertTrue { set.all { it in mapInt } }
    }

    @Test
    fun testIntMapOf() {
        val map = intMapOf(1 to "one", 2 to "two")
        assertEquals(null, map[0])
        assertEquals("one", map[1])
        assertEquals("two", map[2])
        assertEquals(null, map[3])
    }

}
