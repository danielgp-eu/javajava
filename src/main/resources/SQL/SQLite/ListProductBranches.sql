WITH
    CTE__Constants                                                              AS (
        SELECT
            '{"01":"Jan",
                "02":"Feb",
                "03":"Mar",
                "04":"Apr",
                "05":"May",
                "06":"Jun",
                "07":"Jul",
                "08":"Aug",
                "09":"Sep",
                "10":"Oct",
                "11":"Nov",
                "12":"Dec"}'                                                    AS "JSON_MonthNames_Short"
            , '{"0":"Sun",
                "1":"Mon",
                "2":"Tue",
                "3":"Wed",
                "4":"Thu",
                "5":"Fri",
                "6":"Sat"}'                                                     AS "JSON_WeekDays_Short"
    ),
    CTE__INITIAL                                                                AS (
        SELECT
              o.OrganizationName                                                AS "OrganizationName"
            , p.ProductName                                                     AS "ProductName"
            , pb.BranchName                                                     AS "BranchName"
            , o.OrganizationId                                                  AS "OrganizationId"
            , pb.ProductId                                                      AS "ProductId"
            , pb.BranchId                                                       AS "BranchId"
            , JSON_EXTRACT(
                  pb.BranchURLs
                , '$.Releases')                                                 AS "Releases"
            , MAX(bv.ReleaseDate)                                               AS "MaxReleaseDate"
            , MAX(bv.VersionId) OVER
                (PARTITION BY
                      o.OrganizationName
                    , p.ProductName
                    , pb.BranchName
                ORDER BY
                    bv.ReleaseDate DESC
                    , bv.VersionId DESC)                                        AS "Latest VersionId"
        FROM
            organization                                                        AS o
            LEFT JOIN organization_product                                      AS op   ON
                o.OrganizationId    = op.OrganizationId
            LEFT JOIN product                                                   AS p    ON
                op.ProductId        = p.ProductId
            LEFT JOIN product_branch                                            AS pb   ON
                p.ProductId         = pb.ProductId
            LEFT JOIN branch_versions                                           AS bv   ON
                pb.BranchId         = bv.BranchId
        WHERE
            pb.BranchStatus IN ('Active', 'Innovation', 'LTS')
        GROUP BY
              o.OrganizationName
            , p.ProductName
            , pb.ProductId
            , pb.BranchName
            , pb.BranchId
            , pb.BranchURLs
    ),
    CTE__FINAL                                                                  AS (
        SELECT
              ci.OrganizationName                                               AS "OrganizationName"
            , ci.ProductName                                                    AS "ProductName"
            , ci.BranchName                                                     AS "BranchName"
            , ci.OrganizationId                                                 AS "OrganizationId"
            , ci.ProductId                                                      AS "ProductId"
            , ci.BranchId                                                       AS "BranchId"
            , ci.Releases                                                       AS "Releases"
            , bv.VersionCode                                                    AS "Latest release version"
            , JSON_EXTRACT(cc."JSON_WeekDays_Short"
                    , '$."'
                        || STRFTIME('%w', bv.ReleaseDate)
                        || '"') || ', '
                || STRFTIME('%d', bv.ReleaseDate) || ' '
                || JSON_EXTRACT(cc."JSON_MonthNames_Short"
                    , '$."'
                        || STRFTIME('%m', bv.ReleaseDate)
                        || '"') || ' '
                || STRFTIME('%Y', bv.ReleaseDate)                               AS "Latest release date"
            , (JulianDay(date())
                - JulianDay(bv.ReleaseDate))                                    AS "Latest release aging"
            , MAX(bv.VersionId)
                OVER (PARTITION BY
                    bv.BranchId
                    ORDER BY
                        bv.VersionId DESC)                                      AS "VersionId"
            , IFNULL(pl.ProfileName, '#')                                       AS "Profile Name"
            , IFNULL(pl.ProfileId, 999)                                         AS "ProfileId"
        FROM
            CTE__INITIAL                                                        AS ci
            INNER JOIN branch_versions                                          AS bv   ON
                    ci.BranchId                = bv.BranchId
                AND ci."Latest VersionId"      = bv.VersionId
            INNER JOIN CTE__Constants                                           AS cc   ON
                    1 = 1
            LEFT JOIN product_profile                                           AS pp   ON
                ci.ProductId                    = pp.ProductId
            LEFT JOIN profile_list                                              AS pl   ON
                pp.ProfileId                    = pl.ProfileId
    ),
    CTE__Files                                                                  AS (
        SELECT
              "fv"."VersionId"                                                  AS "VersionId"
            , TRIM(GROUP_CONCAT(CASE
                WHEN "fv"."FileType" = 'InstalledIdentifier' THEN
                    "fl"."FileName"
                ELSE
                    ''
                END), ',')                                                      AS "File Installed Name"
            , TRIM(GROUP_CONCAT(CASE
                WHEN "fv"."FileType" = 'Kit' THEN
                    "fl"."FileName"
                ELSE
                    ''
                END), ',')                                                      AS "File Kit Name"
            , TRIM(GROUP_CONCAT(CASE
                WHEN "fv"."FileType" = 'InstalledIdentifier' THEN
                    "fl"."FileId"
                ELSE
                    ''
                END), ',')                                                      AS "File Installed Id"
            , TRIM(GROUP_CONCAT(CASE
                WHEN "fv"."FileType" = 'Kit' THEN
                    "fl"."FileId"
                ELSE
                    ''
                END), ',')                                                      AS "File Kit Id"
        FROM
            "file_version"                                                      AS "fv"
            INNER JOIN "file_list"                                              AS "fl"    ON
                "fv"."FileId" = "fl"."FileId"
        WHERE
            "fv"."FileType"  IN ('InstalledIdentifier', 'Kit')
        GROUP BY
            "fv"."VersionId"
    )
SELECT
      "cf"."OrganizationName"
    , "cf"."ProductName"
    , "cf"."BranchName"
    , "cf"."OrganizationId"
    , "cf"."ProductId"
    , "cf"."BranchId"
    , "cf"."Releases"
    , "cf"."Latest release version"
    , "cf"."Latest release date"
    , "cf"."Latest release aging"
    , "cf"."ProfileId"
    , "cf"."Profile Name"
    , "cf"."VersionId"
    , "ci"."File Installed Name"
    , "ci"."File Installed Id"
    , "ci"."File Kit Name"
    , "ci"."File Kit Id"
FROM
    "CTE__FINAL"                                                                AS "cf"
    INNER JOIN "CTE__Files"                                                     AS "ci"     ON
        "cf"."VersionId"  = "ci"."VersionId"
ORDER BY
      "cf"."Profile Name"
    , "cf"."OrganizationName"
    , "cf"."ProductName"
    , "cf"."BranchName";
