import pytest

"""
v1.2 
2018-11-06: add weight for skipped test

v1.1 (for pytest 3.8)
changelog:
2018-09-24: removed deprecated use of MarkerInfo.

TODO: treat groups separately?

USAGE:

@pytest.mark.incgroup("group1", "group2")

if the test fails, tuple ("group1", "group2") is marked.

@pytest.mark.incgroupdepend("group1", "group2")

if tuple ("group1", "group2") is failed, the test is skipped

@pytest.mark.weight(2) 

sets the weight for the given test, default is 1
"""

_incremental_fails = {}
_kwgroup = 'incgroup'
_kwdepend = 'incgroupdepend'
_kwweight = 'weight'


@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
    # print("pytest_runtest_makereport", item, call)
    # print(item.extra_keyword_matches)
    outcome = yield
    report = outcome.get_result()
    # report.metadata = {'test': 1}
    if _kwweight in item.keywords:
        report = outcome.get_result()
        if report.when == 'call' or (report.when == 'setup' and report.outcome == 'skipped'):
            # print(f"TEST {item.name}, adding weight", float(item.get_closest_marker(_kwweight).args[0]))
            report.test_metadata = {'weight': float(item.get_closest_marker(_kwweight).args[0])}

    kw = _kwgroup
    if kw in item.keywords:
        # print(dir(item))
        # print(item.keywords[kw].args)
        if call.excinfo is not None:
            for arg in item.iter_markers(kw):
                arg = arg.args
                _incremental_fails[arg] = arg
                # print("set failed true:", arg)


def pytest_runtest_setup(item):
    kw = _kwdepend
    if kw in item.keywords:
        # print(kw, item, item.keywords[kw], dir(item.keywords[kw]))

        for arg in item.iter_markers(kw):
            arg = arg.args

            if arg in _incremental_fails:
                pytest.skip("incremental failed:" + str(arg))
                # set other incgroup's
                if _kwgroup in item.keywords:
                    for arg2 in item.keywords[_kwgroup]:
                        _incremental_fails[arg2] = arg2
                        # print("set failed true2:", arg2)
