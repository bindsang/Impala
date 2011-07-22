// Copyright (c) 2011 Cloudera, Inc. All rights reserved.

package com.cloudera.impala.service;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.Assert;

import org.junit.Test;

import com.cloudera.impala.catalog.Catalog;
import com.cloudera.impala.common.ImpalaException;

public class CoordinatorTest {

  // For buffering query results.
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream printStream = new PrintStream(outputStream);
  private final Catalog catalog = Coordinator.createCatalog();

  private void runTestSuccess(String query, int expectedRows)
      throws ImpalaException {
    // start at the beginning of the output stream for every test
    outputStream.reset();
    int syncNumRows = Coordinator.runQuery(query, catalog, false, printStream);
    Assert.assertEquals(expectedRows, syncNumRows);
    int asyncNumRows = Coordinator.runQuery(query, catalog, true, printStream);
    Assert.assertEquals(expectedRows, asyncNumRows);
  }

  private void runTestFailure(String query, int expectedRows) {
    // start at the beginning of the output stream for every test
    outputStream.reset();
    try {
      Coordinator.runQuery(query, catalog, false, printStream);
      fail("Expected query to fail: " + query);
    } catch (Exception e) {
    }
    outputStream.reset();
    try {
      Coordinator.runQuery(query, catalog, true, printStream);
      fail("Expected query to fail: " + query);
    } catch (Exception e) {
    }
  }

  @Test
  public void runTest() throws ImpalaException {
    runTestSuccess("select tinyint_col, int_col, id from alltypessmall", 100);

    // Syntax error.
    runTestFailure("slect tinyint_col from alltypessmall", 100);
    // Unknown column.
    runTestFailure("select tiny from alltypessmall", 100);
    // Different number of results.
    runTestFailure("select tiny from alltypessmall", 80);
  }
}
