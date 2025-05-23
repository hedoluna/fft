/**
 * Fast Fourier Transform - Optimized implementation for arrays of size 2048.
 * 
 * This class provides a highly optimized FFT implementation specifically designed
 * for 2048-element arrays using hardcoded bit-reversal lookup tables.
 * 
 * @author Orlando Selenu (original base algorithm, 2008)
 * @author Engine AI Assistant (optimized implementation, 2025)
 * @since 1.0
 */
public class FFToptim2048 {

    // Precomputed trigonometric lookup tables for 2048-point FFT
    private static final double[] COS_TABLE = new double[1024];
    private static final double[] SIN_TABLE = new double[1024];

    // Hardcoded bit-reversal swap pairs for optimal performance
    private static final int[][] BIT_REVERSAL_PAIRS = {
        {1, 1024},
        {2, 512},
        {3, 1536},
        {4, 256},
        {5, 1280},
        {6, 768},
        {7, 1792},
        {8, 128},
        {9, 1152},
        {10, 640},
        {11, 1664},
        {12, 384},
        {13, 1408},
        {14, 896},
        {15, 1920},
        {16, 64},
        {17, 1088},
        {18, 576},
        {19, 1600},
        {20, 320},
        {21, 1344},
        {22, 832},
        {23, 1856},
        {24, 192},
        {25, 1216},
        {26, 704},
        {27, 1728},
        {28, 448},
        {29, 1472},
        {30, 960},
        {31, 1984},
        {33, 1056},
        {34, 544},
        {35, 1568},
        {36, 288},
        {37, 1312},
        {38, 800},
        {39, 1824},
        {40, 160},
        {41, 1184},
        {42, 672},
        {43, 1696},
        {44, 416},
        {45, 1440},
        {46, 928},
        {47, 1952},
        {48, 96},
        {49, 1120},
        {50, 608},
        {51, 1632},
        {52, 352},
        {53, 1376},
        {54, 864},
        {55, 1888},
        {56, 224},
        {57, 1248},
        {58, 736},
        {59, 1760},
        {60, 480},
        {61, 1504},
        {62, 992},
        {63, 2016},
        {65, 1040},
        {66, 528},
        {67, 1552},
        {68, 272},
        {69, 1296},
        {70, 784},
        {71, 1808},
        {72, 144},
        {73, 1168},
        {74, 656},
        {75, 1680},
        {76, 400},
        {77, 1424},
        {78, 912},
        {79, 1936},
        {81, 1104},
        {82, 592},
        {83, 1616},
        {84, 336},
        {85, 1360},
        {86, 848},
        {87, 1872},
        {88, 208},
        {89, 1232},
        {90, 720},
        {91, 1744},
        {92, 464},
        {93, 1488},
        {94, 976},
        {95, 2000},
        {97, 1072},
        {98, 560},
        {99, 1584},
        {100, 304},
        {101, 1328},
        {102, 816},
        {103, 1840},
        {104, 176},
        {105, 1200},
        {106, 688},
        {107, 1712},
        {108, 432},
        {109, 1456},
        {110, 944},
        {111, 1968},
        {113, 1136},
        {114, 624},
        {115, 1648},
        {116, 368},
        {117, 1392},
        {118, 880},
        {119, 1904},
        {120, 240},
        {121, 1264},
        {122, 752},
        {123, 1776},
        {124, 496},
        {125, 1520},
        {126, 1008},
        {127, 2032},
        {129, 1032},
        {130, 520},
        {131, 1544},
        {132, 264},
        {133, 1288},
        {134, 776},
        {135, 1800},
        {137, 1160},
        {138, 648},
        {139, 1672},
        {140, 392},
        {141, 1416},
        {142, 904},
        {143, 1928},
        {145, 1096},
        {146, 584},
        {147, 1608},
        {148, 328},
        {149, 1352},
        {150, 840},
        {151, 1864},
        {152, 200},
        {153, 1224},
        {154, 712},
        {155, 1736},
        {156, 456},
        {157, 1480},
        {158, 968},
        {159, 1992},
        {161, 1064},
        {162, 552},
        {163, 1576},
        {164, 296},
        {165, 1320},
        {166, 808},
        {167, 1832},
        {169, 1192},
        {170, 680},
        {171, 1704},
        {172, 424},
        {173, 1448},
        {174, 936},
        {175, 1960},
        {177, 1128},
        {178, 616},
        {179, 1640},
        {180, 360},
        {181, 1384},
        {182, 872},
        {183, 1896},
        {184, 232},
        {185, 1256},
        {186, 744},
        {187, 1768},
        {188, 488},
        {189, 1512},
        {190, 1000},
        {191, 2024},
        {193, 1048},
        {194, 536},
        {195, 1560},
        {196, 280},
        {197, 1304},
        {198, 792},
        {199, 1816},
        {201, 1176},
        {202, 664},
        {203, 1688},
        {204, 408},
        {205, 1432},
        {206, 920},
        {207, 1944},
        {209, 1112},
        {210, 600},
        {211, 1624},
        {212, 344},
        {213, 1368},
        {214, 856},
        {215, 1880},
        {217, 1240},
        {218, 728},
        {219, 1752},
        {220, 472},
        {221, 1496},
        {222, 984},
        {223, 2008},
        {225, 1080},
        {226, 568},
        {227, 1592},
        {228, 312},
        {229, 1336},
        {230, 824},
        {231, 1848},
        {233, 1208},
        {234, 696},
        {235, 1720},
        {236, 440},
        {237, 1464},
        {238, 952},
        {239, 1976},
        {241, 1144},
        {242, 632},
        {243, 1656},
        {244, 376},
        {245, 1400},
        {246, 888},
        {247, 1912},
        {249, 1272},
        {250, 760},
        {251, 1784},
        {252, 504},
        {253, 1528},
        {254, 1016},
        {255, 2040},
        {257, 1028},
        {258, 516},
        {259, 1540},
        {261, 1284},
        {262, 772},
        {263, 1796},
        {265, 1156},
        {266, 644},
        {267, 1668},
        {268, 388},
        {269, 1412},
        {270, 900},
        {271, 1924},
        {273, 1092},
        {274, 580},
        {275, 1604},
        {276, 324},
        {277, 1348},
        {278, 836},
        {279, 1860},
        {281, 1220},
        {282, 708},
        {283, 1732},
        {284, 452},
        {285, 1476},
        {286, 964},
        {287, 1988},
        {289, 1060},
        {290, 548},
        {291, 1572},
        {293, 1316},
        {294, 804},
        {295, 1828},
        {297, 1188},
        {298, 676},
        {299, 1700},
        {300, 420},
        {301, 1444},
        {302, 932},
        {303, 1956},
        {305, 1124},
        {306, 612},
        {307, 1636},
        {308, 356},
        {309, 1380},
        {310, 868},
        {311, 1892},
        {313, 1252},
        {314, 740},
        {315, 1764},
        {316, 484},
        {317, 1508},
        {318, 996},
        {319, 2020},
        {321, 1044},
        {322, 532},
        {323, 1556},
        {325, 1300},
        {326, 788},
        {327, 1812},
        {329, 1172},
        {330, 660},
        {331, 1684},
        {332, 404},
        {333, 1428},
        {334, 916},
        {335, 1940},
        {337, 1108},
        {338, 596},
        {339, 1620},
        {341, 1364},
        {342, 852},
        {343, 1876},
        {345, 1236},
        {346, 724},
        {347, 1748},
        {348, 468},
        {349, 1492},
        {350, 980},
        {351, 2004},
        {353, 1076},
        {354, 564},
        {355, 1588},
        {357, 1332},
        {358, 820},
        {359, 1844},
        {361, 1204},
        {362, 692},
        {363, 1716},
        {364, 436},
        {365, 1460},
        {366, 948},
        {367, 1972},
        {369, 1140},
        {370, 628},
        {371, 1652},
        {373, 1396},
        {374, 884},
        {375, 1908},
        {377, 1268},
        {378, 756},
        {379, 1780},
        {380, 500},
        {381, 1524},
        {382, 1012},
        {383, 2036},
        {385, 1036},
        {386, 524},
        {387, 1548},
        {389, 1292},
        {390, 780},
        {391, 1804},
        {393, 1164},
        {394, 652},
        {395, 1676},
        {397, 1420},
        {398, 908},
        {399, 1932},
        {401, 1100},
        {402, 588},
        {403, 1612},
        {405, 1356},
        {406, 844},
        {407, 1868},
        {409, 1228},
        {410, 716},
        {411, 1740},
        {412, 460},
        {413, 1484},
        {414, 972},
        {415, 1996},
        {417, 1068},
        {418, 556},
        {419, 1580},
        {421, 1324},
        {422, 812},
        {423, 1836},
        {425, 1196},
        {426, 684},
        {427, 1708},
        {429, 1452},
        {430, 940},
        {431, 1964},
        {433, 1132},
        {434, 620},
        {435, 1644},
        {437, 1388},
        {438, 876},
        {439, 1900},
        {441, 1260},
        {442, 748},
        {443, 1772},
        {444, 492},
        {445, 1516},
        {446, 1004},
        {447, 2028},
        {449, 1052},
        {450, 540},
        {451, 1564},
        {453, 1308},
        {454, 796},
        {455, 1820},
        {457, 1180},
        {458, 668},
        {459, 1692},
        {461, 1436},
        {462, 924},
        {463, 1948},
        {465, 1116},
        {466, 604},
        {467, 1628},
        {469, 1372},
        {470, 860},
        {471, 1884},
        {473, 1244},
        {474, 732},
        {475, 1756},
        {477, 1500},
        {478, 988},
        {479, 2012},
        {481, 1084},
        {482, 572},
        {483, 1596},
        {485, 1340},
        {486, 828},
        {487, 1852},
        {489, 1212},
        {490, 700},
        {491, 1724},
        {493, 1468},
        {494, 956},
        {495, 1980},
        {497, 1148},
        {498, 636},
        {499, 1660},
        {501, 1404},
        {502, 892},
        {503, 1916},
        {505, 1276},
        {506, 764},
        {507, 1788},
        {509, 1532},
        {510, 1020},
        {511, 2044},
        {513, 1026},
        {515, 1538},
        {517, 1282},
        {518, 770},
        {519, 1794},
        {521, 1154},
        {522, 642},
        {523, 1666},
        {525, 1410},
        {526, 898},
        {527, 1922},
        {529, 1090},
        {530, 578},
        {531, 1602},
        {533, 1346},
        {534, 834},
        {535, 1858},
        {537, 1218},
        {538, 706},
        {539, 1730},
        {541, 1474},
        {542, 962},
        {543, 1986},
        {545, 1058},
        {547, 1570},
        {549, 1314},
        {550, 802},
        {551, 1826},
        {553, 1186},
        {554, 674},
        {555, 1698},
        {557, 1442},
        {558, 930},
        {559, 1954},
        {561, 1122},
        {562, 610},
        {563, 1634},
        {565, 1378},
        {566, 866},
        {567, 1890},
        {569, 1250},
        {570, 738},
        {571, 1762},
        {573, 1506},
        {574, 994},
        {575, 2018},
        {577, 1042},
        {579, 1554},
        {581, 1298},
        {582, 786},
        {583, 1810},
        {585, 1170},
        {586, 658},
        {587, 1682},
        {589, 1426},
        {590, 914},
        {591, 1938},
        {593, 1106},
        {595, 1618},
        {597, 1362},
        {598, 850},
        {599, 1874},
        {601, 1234},
        {602, 722},
        {603, 1746},
        {605, 1490},
        {606, 978},
        {607, 2002},
        {609, 1074},
        {611, 1586},
        {613, 1330},
        {614, 818},
        {615, 1842},
        {617, 1202},
        {618, 690},
        {619, 1714},
        {621, 1458},
        {622, 946},
        {623, 1970},
        {625, 1138},
        {627, 1650},
        {629, 1394},
        {630, 882},
        {631, 1906},
        {633, 1266},
        {634, 754},
        {635, 1778},
        {637, 1522},
        {638, 1010},
        {639, 2034},
        {641, 1034},
        {643, 1546},
        {645, 1290},
        {646, 778},
        {647, 1802},
        {649, 1162},
        {651, 1674},
        {653, 1418},
        {654, 906},
        {655, 1930},
        {657, 1098},
        {659, 1610},
        {661, 1354},
        {662, 842},
        {663, 1866},
        {665, 1226},
        {666, 714},
        {667, 1738},
        {669, 1482},
        {670, 970},
        {671, 1994},
        {673, 1066},
        {675, 1578},
        {677, 1322},
        {678, 810},
        {679, 1834},
        {681, 1194},
        {683, 1706},
        {685, 1450},
        {686, 938},
        {687, 1962},
        {689, 1130},
        {691, 1642},
        {693, 1386},
        {694, 874},
        {695, 1898},
        {697, 1258},
        {698, 746},
        {699, 1770},
        {701, 1514},
        {702, 1002},
        {703, 2026},
        {705, 1050},
        {707, 1562},
        {709, 1306},
        {710, 794},
        {711, 1818},
        {713, 1178},
        {715, 1690},
        {717, 1434},
        {718, 922},
        {719, 1946},
        {721, 1114},
        {723, 1626},
        {725, 1370},
        {726, 858},
        {727, 1882},
        {729, 1242},
        {731, 1754},
        {733, 1498},
        {734, 986},
        {735, 2010},
        {737, 1082},
        {739, 1594},
        {741, 1338},
        {742, 826},
        {743, 1850},
        {745, 1210},
        {747, 1722},
        {749, 1466},
        {750, 954},
        {751, 1978},
        {753, 1146},
        {755, 1658},
        {757, 1402},
        {758, 890},
        {759, 1914},
        {761, 1274},
        {763, 1786},
        {765, 1530},
        {766, 1018},
        {767, 2042},
        {769, 1030},
        {771, 1542},
        {773, 1286},
        {775, 1798},
        {777, 1158},
        {779, 1670},
        {781, 1414},
        {782, 902},
        {783, 1926},
        {785, 1094},
        {787, 1606},
        {789, 1350},
        {790, 838},
        {791, 1862},
        {793, 1222},
        {795, 1734},
        {797, 1478},
        {798, 966},
        {799, 1990},
        {801, 1062},
        {803, 1574},
        {805, 1318},
        {807, 1830},
        {809, 1190},
        {811, 1702},
        {813, 1446},
        {814, 934},
        {815, 1958},
        {817, 1126},
        {819, 1638},
        {821, 1382},
        {822, 870},
        {823, 1894},
        {825, 1254},
        {827, 1766},
        {829, 1510},
        {830, 998},
        {831, 2022},
        {833, 1046},
        {835, 1558},
        {837, 1302},
        {839, 1814},
        {841, 1174},
        {843, 1686},
        {845, 1430},
        {846, 918},
        {847, 1942},
        {849, 1110},
        {851, 1622},
        {853, 1366},
        {855, 1878},
        {857, 1238},
        {859, 1750},
        {861, 1494},
        {862, 982},
        {863, 2006},
        {865, 1078},
        {867, 1590},
        {869, 1334},
        {871, 1846},
        {873, 1206},
        {875, 1718},
        {877, 1462},
        {878, 950},
        {879, 1974},
        {881, 1142},
        {883, 1654},
        {885, 1398},
        {887, 1910},
        {889, 1270},
        {891, 1782},
        {893, 1526},
        {894, 1014},
        {895, 2038},
        {897, 1038},
        {899, 1550},
        {901, 1294},
        {903, 1806},
        {905, 1166},
        {907, 1678},
        {909, 1422},
        {911, 1934},
        {913, 1102},
        {915, 1614},
        {917, 1358},
        {919, 1870},
        {921, 1230},
        {923, 1742},
        {925, 1486},
        {926, 974},
        {927, 1998},
        {929, 1070},
        {931, 1582},
        {933, 1326},
        {935, 1838},
        {937, 1198},
        {939, 1710},
        {941, 1454},
        {943, 1966},
        {945, 1134},
        {947, 1646},
        {949, 1390},
        {951, 1902},
        {953, 1262},
        {955, 1774},
        {957, 1518},
        {958, 1006},
        {959, 2030},
        {961, 1054},
        {963, 1566},
        {965, 1310},
        {967, 1822},
        {969, 1182},
        {971, 1694},
        {973, 1438},
        {975, 1950},
        {977, 1118},
        {979, 1630},
        {981, 1374},
        {983, 1886},
        {985, 1246},
        {987, 1758},
        {989, 1502},
        {991, 2014},
        {993, 1086},
        {995, 1598},
        {997, 1342},
        {999, 1854},
        {1001, 1214},
        {1003, 1726},
        {1005, 1470},
        {1007, 1982},
        {1009, 1150},
        {1011, 1662},
        {1013, 1406},
        {1015, 1918},
        {1017, 1278},
        {1019, 1790},
        {1021, 1534},
        {1023, 2046},
        {1027, 1537},
        {1029, 1281},
        {1031, 1793},
        {1033, 1153},
        {1035, 1665},
        {1037, 1409},
        {1039, 1921},
        {1041, 1089},
        {1043, 1601},
        {1045, 1345},
        {1047, 1857},
        {1049, 1217},
        {1051, 1729},
        {1053, 1473},
        {1055, 1985},
        {1059, 1569},
        {1061, 1313},
        {1063, 1825},
        {1065, 1185},
        {1067, 1697},
        {1069, 1441},
        {1071, 1953},
        {1073, 1121},
        {1075, 1633},
        {1077, 1377},
        {1079, 1889},
        {1081, 1249},
        {1083, 1761},
        {1085, 1505},
        {1087, 2017},
        {1091, 1553},
        {1093, 1297},
        {1095, 1809},
        {1097, 1169},
        {1099, 1681},
        {1101, 1425},
        {1103, 1937},
        {1107, 1617},
        {1109, 1361},
        {1111, 1873},
        {1113, 1233},
        {1115, 1745},
        {1117, 1489},
        {1119, 2001},
        {1123, 1585},
        {1125, 1329},
        {1127, 1841},
        {1129, 1201},
        {1131, 1713},
        {1133, 1457},
        {1135, 1969},
        {1139, 1649},
        {1141, 1393},
        {1143, 1905},
        {1145, 1265},
        {1147, 1777},
        {1149, 1521},
        {1151, 2033},
        {1155, 1545},
        {1157, 1289},
        {1159, 1801},
        {1163, 1673},
        {1165, 1417},
        {1167, 1929},
        {1171, 1609},
        {1173, 1353},
        {1175, 1865},
        {1177, 1225},
        {1179, 1737},
        {1181, 1481},
        {1183, 1993},
        {1187, 1577},
        {1189, 1321},
        {1191, 1833},
        {1195, 1705},
        {1197, 1449},
        {1199, 1961},
        {1203, 1641},
        {1205, 1385},
        {1207, 1897},
        {1209, 1257},
        {1211, 1769},
        {1213, 1513},
        {1215, 2025},
        {1219, 1561},
        {1221, 1305},
        {1223, 1817},
        {1227, 1689},
        {1229, 1433},
        {1231, 1945},
        {1235, 1625},
        {1237, 1369},
        {1239, 1881},
        {1243, 1753},
        {1245, 1497},
        {1247, 2009},
        {1251, 1593},
        {1253, 1337},
        {1255, 1849},
        {1259, 1721},
        {1261, 1465},
        {1263, 1977},
        {1267, 1657},
        {1269, 1401},
        {1271, 1913},
        {1275, 1785},
        {1277, 1529},
        {1279, 2041},
        {1283, 1541},
        {1287, 1797},
        {1291, 1669},
        {1293, 1413},
        {1295, 1925},
        {1299, 1605},
        {1301, 1349},
        {1303, 1861},
        {1307, 1733},
        {1309, 1477},
        {1311, 1989},
        {1315, 1573},
        {1319, 1829},
        {1323, 1701},
        {1325, 1445},
        {1327, 1957},
        {1331, 1637},
        {1333, 1381},
        {1335, 1893},
        {1339, 1765},
        {1341, 1509},
        {1343, 2021},
        {1347, 1557},
        {1351, 1813},
        {1355, 1685},
        {1357, 1429},
        {1359, 1941},
        {1363, 1621},
        {1367, 1877},
        {1371, 1749},
        {1373, 1493},
        {1375, 2005},
        {1379, 1589},
        {1383, 1845},
        {1387, 1717},
        {1389, 1461},
        {1391, 1973},
        {1395, 1653},
        {1399, 1909},
        {1403, 1781},
        {1405, 1525},
        {1407, 2037},
        {1411, 1549},
        {1415, 1805},
        {1419, 1677},
        {1423, 1933},
        {1427, 1613},
        {1431, 1869},
        {1435, 1741},
        {1437, 1485},
        {1439, 1997},
        {1443, 1581},
        {1447, 1837},
        {1451, 1709},
        {1455, 1965},
        {1459, 1645},
        {1463, 1901},
        {1467, 1773},
        {1469, 1517},
        {1471, 2029},
        {1475, 1565},
        {1479, 1821},
        {1483, 1693},
        {1487, 1949},
        {1491, 1629},
        {1495, 1885},
        {1499, 1757},
        {1503, 2013},
        {1507, 1597},
        {1511, 1853},
        {1515, 1725},
        {1519, 1981},
        {1523, 1661},
        {1527, 1917},
        {1531, 1789},
        {1535, 2045},
        {1543, 1795},
        {1547, 1667},
        {1551, 1923},
        {1555, 1603},
        {1559, 1859},
        {1563, 1731},
        {1567, 1987},
        {1575, 1827},
        {1579, 1699},
        {1583, 1955},
        {1587, 1635},
        {1591, 1891},
        {1595, 1763},
        {1599, 2019},
        {1607, 1811},
        {1611, 1683},
        {1615, 1939},
        {1623, 1875},
        {1627, 1747},
        {1631, 2003},
        {1639, 1843},
        {1643, 1715},
        {1647, 1971},
        {1655, 1907},
        {1659, 1779},
        {1663, 2035},
        {1671, 1803},
        {1679, 1931},
        {1687, 1867},
        {1691, 1739},
        {1695, 1995},
        {1703, 1835},
        {1711, 1963},
        {1719, 1899},
        {1723, 1771},
        {1727, 2027},
        {1735, 1819},
        {1743, 1947},
        {1751, 1883},
        {1759, 2011},
        {1767, 1851},
        {1775, 1979},
        {1783, 1915},
        {1791, 2043},
        {1807, 1927},
        {1815, 1863},
        {1823, 1991},
        {1839, 1959},
        {1847, 1895},
        {1855, 2023},
        {1871, 1943},
        {1887, 2007},
        {1903, 1975},
        {1919, 2039},
        {1951, 1999},
        {1983, 2031}
    };

    static {
        // Initialize trigonometric lookup tables
        for (int i = 0; i < 1024; i++) {
            double angle = 2.0 * Math.PI * i / 2048.0;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }

    /**
     * Performs Fast Fourier Transform optimized for 2048-element arrays.
     * 
     * @param inputReal array of exactly 2048 real values
     * @param inputImag array of exactly 2048 imaginary values
     * @param DIRECT true for forward transform, false for inverse
     * @return array of length 4096 with interleaved real and imaginary results
     */
    public static double[] fft(final double[] inputReal, double[] inputImag, boolean DIRECT) {
        // Validate input size
        if (inputReal.length != 2048 || inputImag.length != 2048) {
            System.out.println("ERROR: Input arrays must be exactly size 2048");
            return new double[0];
        }

        // Hardcoded parameters for size 2048
        final int n = 2048;
        final int nu = 11;
        int n2 = 1024;
        int nu1 = 10;
        double[] xReal = new double[n];
        double[] xImag = new double[n];
        double tReal, tImag;

        // Copy input arrays
        System.arraycopy(inputReal, 0, xReal, 0, n);
        System.arraycopy(inputImag, 0, xImag, 0, n);

        // FFT butterfly computation stages
        int k = 0;
        for (int l = 1; l <= nu; l++) {
            while (k < n) {
                for (int i = 1; i <= n2; i++) {
                    int p_index = k >> nu1;
                    double c, s;
                    if (p_index < 1024) {
                        c = DIRECT ? COS_TABLE[p_index] : COS_TABLE[p_index];
                        s = DIRECT ? -SIN_TABLE[p_index] : SIN_TABLE[p_index];
                    } else {
                        c = 1.0; s = 0.0;
                    }
                    tReal = xReal[k + n2] * c + xImag[k + n2] * s;
                    tImag = xImag[k + n2] * c - xReal[k + n2] * s;
                    xReal[k + n2] = xReal[k] - tReal;
                    xImag[k + n2] = xImag[k] - tImag;
                    xReal[k] += tReal;
                    xImag[k] += tImag;
                    k++;
                }
                k += n2;
            }
            k = 0; nu1--; n2 /= 2;
        }

        // Final bit-reversal reordering using hardcoded lookup table
        for (int[] pair : BIT_REVERSAL_PAIRS) {
            int i = pair[0], j = pair[1];
            tReal = xReal[i]; tImag = xImag[i];
            xReal[i] = xReal[j]; xImag[i] = xImag[j];
            xReal[j] = tReal; xImag[j] = tImag;
        }

        // Generate interleaved output with normalization
        double[] result = new double[4096];
        double radice = 1.0 / Math.sqrt(n);
        for (int i = 0; i < n; i++) {
            result[2 * i] = xReal[i] * radice;
            result[2 * i + 1] = xImag[i] * radice;
        }
        return result;
    }
}
