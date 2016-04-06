{
    "fragments": [
        { 
            "operators": [
                   {
                        "opType": "DbQueryScan",
                        "sql": "SELECT input.\"iOrder\", input.mass, input.x, input.y, input.z, input.vx, input.vy, input.vz, input.rho, input.temp, input.hsmooth, input.metals, input.tform, input.eps, input.phi, input.grp, input.type \nFROM (SELECT \"public:adhoc:cosmo512\".\"iOrder\" AS \"iOrder\", \"public:adhoc:cosmo512\".mass AS mass, \"public:adhoc:cosmo512\".x AS x, \"public:adhoc:cosmo512\".y AS y, \"public:adhoc:cosmo512\".z AS z, \"public:adhoc:cosmo512\".vx AS vx, \"public:adhoc:cosmo512\".vy AS vy, \"public:adhoc:cosmo512\".vz AS vz, \"public:adhoc:cosmo512\".rho AS rho, \"public:adhoc:cosmo512\".temp AS temp, \"public:adhoc:cosmo512\".hsmooth AS hsmooth, \"public:adhoc:cosmo512\".metals AS metals, \"public:adhoc:cosmo512\".tform AS tform, \"public:adhoc:cosmo512\".eps AS eps, \"public:adhoc:cosmo512\".phi AS phi, \"public:adhoc:cosmo512\".grp AS grp, \"public:adhoc:cosmo512\".type AS type \nFROM \"public:adhoc:cosmo512\") AS input \nWHERE input.\"iOrder\" > 3941800",
                        "opName": "MyriaQueryScan('SELECT input.\"iOrder\", input.mass, input.x, input.y, input.z, input.vx, input.vy, input.vz, input.rho, input.temp, input.hsmooth, input.metals, input.tform, input.eps, input.phi, input.grp, input.type \\nFROM (SELECT \"public:adhoc:cosmo512\".\"iOrder\" AS \"iOrder\", \"public:adhoc:cosmo512\".mass AS mass, \"public:adhoc:cosmo512\".x AS x, \"public:adhoc:cosmo512\".y AS y, \"public:adhoc:cosmo512\".z AS z, \"public:adhoc:cosmo512\".vx AS vx, \"public:adhoc:cosmo512\".vy AS vy, \"public:adhoc:cosmo512\".vz AS vz, \"public:adhoc:cosmo512\".rho AS rho, \"public:adhoc:cosmo512\".temp AS temp, \"public:adhoc:cosmo512\".hsmooth AS hsmooth, \"public:adhoc:cosmo512\".metals AS metals, \"public:adhoc:cosmo512\".tform AS tform, \"public:adhoc:cosmo512\".eps AS eps, \"public:adhoc:cosmo512\".phi AS phi, \"public:adhoc:cosmo512\".grp AS grp, \"public:adhoc:cosmo512\".type AS type \\nFROM \"public:adhoc:cosmo512\") AS input \\nWHERE input.iOrder > 3941800')",
                        "opId": 1,
                        "schema": {
                            "columnTypes": [
                                "LONG_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "LONG_TYPE",
                                "STRING_TYPE"
                            ],
                            "columnNames": [
                                "iOrder",
                                "mass",
                                "x",
                                "y",
                                "z",
                                "vx",
                                "vy",
                                "vz",
                                "rho",
                                "temp",
                                "hsmooth",
                                "metals",
                                "tform",
                                "eps",
                                "phi",
                                "grp",
                                "type"
                            ]
                        }
                    },
                {
                    "argChild": 1,
                    "argPf": {
                        "index":0,
                        "type": "SingleFieldHash"
                    },
                    "opName": "Scatter",
                    "opId": 2,
                    "opType": "CacheShuffleProducer"
                }
            ]
        },
        {
            "operators": [
                {
                    "argOperatorId": 2,
                    "opName": "Gather",
                    "opId": 3,
                    "opType": "CacheShuffleConsumer"
                },
                {
                     "opName": "V0", 
                     "opType": "CacheScan", 
                     "schema": {
                            "columnTypes": [
                                "LONG_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "DOUBLE_TYPE",
                                "LONG_TYPE",
                                "STRING_TYPE"
                            ],
                            "columnNames": [
                                "iOrder",
                                "mass",
                                "x",
                                "y",
                                "z",
                                "vx",
                                "vy",
                                "vz",
                                "rho",
                                "temp",
                                "hsmooth",
                                "metals",
                                "tform",
                                "eps",
                                "phi",
                                "grp",
                                "type"
                            ]
                        },
                        "opId": 4 
                },
                {
                     "opName": "V0", 
                     "opType": "UnionAll", 
                     "opId": 5, 
                     "argChildren": [3,4]
                },
                {
                    "opName": "V0",
                    "opType": "DbInsert",
                    "argOverwriteTable": true,
                    "relationKey": {
                        "userName": "public",
                        "relationName": "union_cache0",
                        "programName": "adhoc"
                    },
                    "argChild": 5,
                    "opId": 6
                }
            ]
        }
    ],
    "logicalRa": "cache ShuffleConsumer union",
    "rawQuery": "cache ShuffleConsumer union"
}