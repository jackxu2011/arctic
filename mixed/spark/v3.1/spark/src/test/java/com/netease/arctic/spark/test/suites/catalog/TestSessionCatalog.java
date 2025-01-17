/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netease.arctic.spark.test.suites.catalog;

import com.netease.arctic.ams.api.TableFormat;
import com.netease.arctic.spark.table.ArcticSparkTable;
import com.netease.arctic.spark.test.MixedTableTestBase;
import com.netease.arctic.spark.test.SparkTestContext;
import com.netease.arctic.table.PrimaryKeySpec;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.relocated.com.google.common.collect.ImmutableMap;
import org.apache.iceberg.types.Types;
import org.apache.spark.sql.catalyst.analysis.NoSuchTableException;
import org.apache.spark.sql.connector.catalog.Table;
import org.apache.spark.sql.connector.catalog.TableCatalog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TestSessionCatalog extends MixedTableTestBase {

  public static final Schema schema =
      new Schema(
          Types.NestedField.required(1, "id", Types.IntegerType.get()),
          Types.NestedField.required(2, "data", Types.StringType.get()),
          Types.NestedField.required(3, "pt", Types.StringType.get()));
  public static final PrimaryKeySpec pkSpec =
      PrimaryKeySpec.builderFor(schema).addColumn("id").build();
  public static final PartitionSpec ptSpec =
      PartitionSpec.builderFor(schema).identity("pt").build();

  @Override
  protected Map<String, String> sparkSessionConfig() {
    return ImmutableMap.of(
        "spark.sql.catalog.spark_catalog",
        SparkTestContext.SESSION_CATALOG_IMPL,
        "spark.sql.catalog.spark_catalog.uri",
        context.amsCatalogUrl(TableFormat.MIXED_ICEBERG));
  }

  @Test
  public void testLoadTables() throws NoSuchTableException {
    createTarget(schema, builder -> builder.withPrimaryKeySpec(pkSpec).withPartitionSpec(ptSpec));

    TableCatalog sessionCatalog =
        (TableCatalog) spark().sessionState().catalogManager().catalog(SPARK_SESSION_CATALOG);

    Table table = sessionCatalog.loadTable(target().toSparkIdentifier());
    Assertions.assertTrue(table instanceof ArcticSparkTable);
  }
}
