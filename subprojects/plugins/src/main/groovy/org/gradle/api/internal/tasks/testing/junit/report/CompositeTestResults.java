/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.tasks.testing.junit.report;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

import static org.gradle.api.tasks.testing.TestResult.ResultType;

public abstract class CompositeTestResults extends TestResultModel {
    private final CompositeTestResults parent;
    private int tests;
    private final Set<TestResult> failures = new TreeSet<TestResult>();
    private long duration;
    private int ignored;

    protected CompositeTestResults(CompositeTestResults parent) {
        this.parent = parent;
    }

    public CompositeTestResults getParent() {
        return parent;
    }

    public abstract String getBaseUrl();

    public String getUrlTo(CompositeTestResults model) {
        String otherUrl = model.getBaseUrl();
        String thisUrl = getBaseUrl();

        int maxPos = Math.min(thisUrl.length(), otherUrl.length());
        int endPrefix = 0;
        while (endPrefix < maxPos) {
            int endA = thisUrl.indexOf('/', endPrefix);
            int endB = otherUrl.indexOf('/', endPrefix);
            if (endA != endB || endA < 0) {
                break;
            }
            if (!thisUrl.regionMatches(endPrefix, otherUrl, endPrefix, endA - endPrefix)) {
                break;
            }
            endPrefix = endA + 1;
        }

        StringBuilder result = new StringBuilder();
        int endA = endPrefix;
        while (endA < thisUrl.length()) {
            int pos = thisUrl.indexOf('/', endA);
            if (pos < 0) {
                break;
            }
            result.append("../");
            endA = pos + 1;
        }
        result.append(otherUrl.substring(endPrefix));

        return result.toString();
    }

    public int getTestCount() {
        return tests;
    }

    public int getFailureCount() {
        return failures.size();
    }

    public int getIgnoredCount() {
        return ignored;
    }

    public int getRunTestCount() {
        return tests - ignored;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String getFormattedDuration() {
        return getTestCount() == 0 ? "-" : super.getFormattedDuration();
    }

    public Set<TestResult> getFailures() {
        return failures;
    }

    public ResultType getResultType() {
        return failures.isEmpty() ? ResultType.SUCCESS : ResultType.FAILURE;
    }

    public String getFormattedSuccessRate() {
        Number successRate = getSuccessRate();
        if (successRate == null) {
            return "-";
        }
        return successRate + "%";
    }

    public Number getSuccessRate() {
        if (getRunTestCount() == 0) {
            return null;
        }

        BigDecimal runTests = BigDecimal.valueOf(getRunTestCount());
        BigDecimal successful = BigDecimal.valueOf(getRunTestCount() - getFailureCount());

        return successful.divide(runTests, 2, BigDecimal.ROUND_DOWN).multiply(BigDecimal.valueOf(100)).intValue();
    }

    protected void failed(TestResult failedTest) {
        failures.add(failedTest);
        if (parent != null) {
            parent.failed(failedTest);
        }
    }

    protected void addIgnored() {
        ignored++;
        if (parent != null) {
            parent.addIgnored();
        }
    }

    protected TestResult addTest(TestResult test) {
        tests++;
        duration += test.getDuration();
        return test;
    }
}
