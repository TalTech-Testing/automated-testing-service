import json
import logging
import logging.handlers
import os
import subprocess
import sys

from shutil import copy2, Error, copystat

import re

VERSION = 2

#from hodorcommon.common import get_logger, get_source_list
import xml.etree.ElementTree as ET


# from hodorcommon.common import get_logger

# let's store log session key to be accessible for different get_logger calls
log_session = None
def get_logger(session=None, name='log', log_path=None):
    """
    Get the logger.
    :param session: session key
    :param name: filename
    :param log_path: path, default /tmp
    :return: logging.LoggerAdapter
    """
    global log_session
    logger = logging.getLogger('logger_component')
    # TODO: conf
    logger.setLevel(logging.DEBUG)
    if not logger.handlers:
        if log_path is None:
            log_path = os.path.join('/tmp', name + '.log')
        os.makedirs(os.path.dirname(log_path), exist_ok=True)
        #fh = logging.FileHandler(log_path)
        # 100MB
        fh = logging.handlers.RotatingFileHandler(log_path, maxBytes=100 * 1024 * 1024, backupCount=10)
        fh.setLevel(logging.DEBUG)
        formatter = logging.Formatter('%(levelname)s - %(asctime)s - %(session)s - %(message)s')
        fh.setFormatter(formatter)
        logger.addHandler(fh)
        #lh = RPCLogHandler()
        #logger.addHandler(lh)

    extra = {}
    if session is not None:
        extra = {'session': session}
        log_session = session
    else:
        if log_session is not None:
            extra = {'session': log_session}
        else:
            # use some value for logger to exist
            extra = {'session': 1}

    adapter = logging.LoggerAdapter(logger, extra)
    return adapter


#from hodorcommon.common import get_source_list
def get_source_list(project_dir,
                    allowed_extensions=['java', 'txt', 'py', 'cpp', 'c', 'xml', 'html', 'xml', 'fxml', 'pl']):
    """ Return list of source dicts in form [{'path':.., 'content':..},...] """
    lst = []
    for (root, dirnames, files) in os.walk(project_dir):
        for filename in files:
            # source.txt?
            fname = os.path.join(root, filename)
            fnamepart = os.path.splitext(fname)
            ext = None
            if len(fnamepart) > 1 and len(fnamepart[1]) > 1:
                ext = fnamepart[1][1:].lower()
            if ext not in allowed_extensions:
                continue
            with open(fname, 'r', encoding='utf-8') as f:

                path = fname.replace(project_dir + '/', '')
                contents = f.read()
                if path and contents:
                    d = {'path': path, 'contents': contents, 'isTest': False}
                    lst.append(d)
    return lst


def sh(cmd):
    """
    Executes the command.
    Returns (returncode, stdout, stderr, Popen object)
    """
    # print cmd
    p = subprocess.Popen(cmd,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.PIPE,
                         shell=True)
    out, err = p.communicate()
    # print 'exit code', p.returncode
    return p.returncode, out.decode('utf-8'), err.decode('utf-8'), p


def copyfiles(src, dst, ignore=None):
    """
    https://docs.python.org/2/library/shutil.html#copytree-example
    with some modifications (destination folder can exist)
    """
    names = sorted(os.listdir(src))
    files = []
    if ignore is not None:
        ignored_names = ignore(src, names)
    else:
        ignored_names = set()

    if not os.path.exists(dst):
        os.makedirs(dst)
    errors = []
    for name in names:
        if name in ignored_names:
            continue
        srcname = os.path.join(src, name)
        dstname = os.path.join(dst, name)
        try:
            if os.path.isdir(srcname):
                files += copyfiles(srcname, dstname, ignore)
            else:
                copy2(srcname, dstname)
                files += [dstname]
                # XXX What about devices, sockets etc.?
        except (IOError, os.error) as why:
            errors.append((srcname, dstname, str(why)))
        # catch the Error from the recursive copytree so that we can
        # continue with other files
        except Error as err:
            errors.extend(err.args[0])
    try:
        copystat(src, dst)
    except WindowsError:
        # can't copy file access times on Windows
        pass
    except OSError as why:
        errors.extend((src, dst, str(why)))
    if errors:
        raise Error(errors)
    return files


def validate_flake8(filename):
    logger = get_logger()
    cmd = 'flake8 --statistics --ignore=E501,W503 --disable-noqa --max-complexity 10 "' + filename + '"'
    (exitval, out, err, p) = sh(cmd)
    flake8_violation = False
    if len(err) > 0 or len(out) > 0:
        logger.debug("FLAKE8 error output: " + str(err))
        m_feedback = re.search(r'^(.*)/.*:', out, re.MULTILINE)
        if m_feedback is not None:
            out = out.replace(m_feedback.group(1) + "/", "")
        flake8_violation = True
    return (out, flake8_violation)


def validate_pep257(filename):
    logger = get_logger()
    cmd = 'pep257 "' + filename + '"'
    (exitval, out, err, p) = sh(cmd)
    pep257_violation = False
    if len(err) > 0 or len(out) > 0:
        logger.debug("PEP257 error output: " + str(err))
        m_feedback = re.search(r'^(.*)/.*:', err, re.MULTILINE)
        if m_feedback is not None:
            err = err.replace(m_feedback.group(1) + "/", "")
        pep257_violation = True
    return (err, pep257_violation)

def test(json_string):
    data = json.loads(json_string)
    """
    data has:
    "contentRoot" - points to the folder where content (source) is stored
    "testRoot" - folder where the test files are stored
    "extra" - additional information:
    
      - minimal - show just "received" as feedback
      - stylecheck - run stylecheck
      - enable_socket - socket calls are enabled (disabled by default)

    All the files in contentRoot and testRoot should remain unchanged
    (that way it is possible to re-run the tester)
    """
    session = None
    if 'session' in data:
        session = data['session']
    logger = get_logger(session)
    logger.debug('start now pytest')
    # debug for localhost
    logger.logger.addHandler(logging.StreamHandler())
    """
    what to show in email?
    "normal" - default
    "minimal" - just "received"
    """
    email_feedback = 'normal'
    try:
        sourcefrom = data['contentRoot']
        testfrom = data['testRoot']
        # read possible extra field
        extra = ''
        if 'extra' in data:
            extra = data['extra']

        # one dir up from content root, should end up in "/host"
        testroot = os.path.dirname(data['contentRoot'].rstrip('/'))
        testpath = os.path.join(testroot, 'pytest_tmp')

        # copy both contents and tests to testpath
        sourcefiles = copyfiles(sourcefrom, testpath)

        # copy helpers
        helperfiles = copyfiles("helpers", testpath)

        if 'minimal' in extra:
            # minimal feedback
            email_feedback = 'minimal'

        # do checkstyle here, otherwise something may be overwritten
        checkstyle_output = ""
        checkstyle_result = None
        if 'stylecheck' in extra or 'checkstyle' in extra:
            checkstyle_output += "Style conventions checker results:\n\n"
            flake8_violation = False
            pep257_violation = False
            flake8_disabled = False
            logger.info("Checking style!")
            for sourcefile in sourcefiles:
                logger.debug("Checking style for " + sourcefile)
                if sourcefile[-3:] != '.py':
                    logger.debug('Not a py-file')
                    continue
                logger.debug("Checking flake8!")
                (flake8_feedback, violation) = validate_flake8(sourcefile)
                checkstyle_output += "PEP8 stylecheck:\n" + flake8_feedback
                if violation:
                    flake8_violation = True
                logger.debug("Checking pep257!")
                (pep257_feedback, violation) = validate_pep257(sourcefile)
                checkstyle_output += "\nPEP257 stylecheck:\n" + pep257_feedback
                if violation:
                    pep257_violation = True
                # Check whether the code has codechecker disable commands
                try:
                    with open(sourcefile, 'r', encoding='utf-8') as content_file:
                        content = content_file.read()
                        if "# flake8: noqa" in content or "# noqa" in content:
                            flake8_disabled = True
                            logger.warning("noqa tag found in file!")
                except:
                    logger.exception("Unable to open file for reading!")
                    checkstyle_output = "Unable to open file for reading"
                if flake8_disabled:
                    checkstyle_output += "\n\nStyle checker disabling directives found in source code! Please remove (noqa) and try again!\n\n"
                else:
                    if not flake8_violation:
                        checkstyle_output += "Code conforms to PEP8 (coding style) guidelines! Good job!\n"
                    if not pep257_violation:
                        checkstyle_output += "Code conforms to PEP257 (docstring conventions) guidelines! Great work!\n"
                        checkstyle_output += "\n"
            # Add results to final results
            logger.debug("Adding style results to array!")
            if not flake8_violation and not pep257_violation and not flake8_disabled:
                checkstyle_result = {'count': 0, 'code': 101, "errors": [],
                                     "identifier": "CHECKSTYLE",
                                     "result": "SUCCESS"}
                checkstyle_output += "Style percentage: 100%\n"
            else:
                checkstyle_result = {'count': 1, 'code': 101,
                                     'errors': [{'message': 'Code does not conform to style guidelines'}],
                                     "identifier": "CHECKSTYLE",
                                     "result": "SUCCESS"}
                checkstyle_output += "Style percentage: 0%\n"

        testfiles = copyfiles(testfrom, testpath)

        pytest_output_file = os.path.join(testroot, 'pytest_output.json')
        pytest_output_xml = os.path.join(testroot, 'pytext_output.xml')
        resultfile = os.path.join(testroot, 'output.json')

        timeout = 60
        # TODO: fix: use concrete time from extra
        if 'longtimeout' in extra:
            timeout = 600
        # sent to worker
        results_list = []
        results_output = ""
        extra_output = 'extra'

        grade_number = 1
        results_total_count = 0
        results_total_passed = 0

        exitval = 0

        # source
        source_list = []
        try:
            logger.debug('reading source from:' + sourcefrom)
            source_list = get_source_list(sourcefrom, allowed_extensions=['py'])
        except:
            logger.exception("Error while getting source list")

        is_error = False # it True, skip the rest

        # cd to testpath (to read files from test)
        os.chdir(testpath)
        # let's check whether source compiles
        for sourcefile in sourcefiles:
            if sourcefile[-3:] != '.py': continue # only py files
            cmd = 'python3.7 -m py_compile "' + sourcefile + '"'
            logger.debug('cmd:' + cmd)
            (exitval, out, err, _) = sh(cmd)
            logger.debug('output:' + str(out))
            logger.debug('stderr:' + str(err))
            if len(err) > 0:
                m_filename = re.finditer(r'File.*"(.*)"', err, re.MULTILINE)
                if m_filename is not None:
                    for fail in m_filename:
                        err = err.replace(fail.group(1), "!?!?!?!")
                stroutput = "Syntax error detected\n\n" + err
                result = {'output': stroutput,
                          'extra': "",
                          'results': [{'percentage': 0.0, 'grade_type_code': 1,
                                       'output': stroutput,
                                       'stdout': out, 'stderr': err}],
                          # [{'percent': 100.0, 'title': 'style', 'output': 'Code conforms to style guidelines'} ... }
                          'percent': 0,
                          'files': source_list
                          }
                is_error = True
                with open(resultfile, 'w', encoding='utf-8') as f:
                    json.dump(result, f)
                break

        if not is_error and checkstyle_output:
            # checkstyle
            results_output += checkstyle_output + "\n\n"
            results_list.append(checkstyle_result)

        # add compiler result
        results_list.append({"code": 102, "diagnosticList": [], "identifier": "COMPILER", "result": "SUCCESS"})
        # add files
        results_list.append({"code": 103, "files": source_list, "identifier": "FILEWRITER", "result": "SUCCESS"})
        # includes all the test files
        testfile_list = []

        if not is_error:
            for testfile in testfiles:
                logger.debug('processing test file:' + str(testfile))
                if testfile[-3:] != '.py': continue # only py files

                # try to check if the file is test file
                correct = False
                testfile_name = os.path.basename(testfile)
                # conf file
                if testfile_name == 'conftest.py': continue
                # hack for 2018 shortest_path folder
                if 'test' in testfile_name and 'shortest' not in testfile_name:
                    logger.debug('found test in filename {} (path: {})'.format(testfile_name, testfile))
                    correct = True
                if not correct:
                    # let's check for pytest import
                    try:
                        with open(testfile, 'r') as f:
                            contents = f.read()
                            if re.search("import\s+pytest", contents):
                                logger.debug('the file have import pytest in it')
                                correct = True
                    except UnicodeDecodeError:
                        with open(testfile, 'r', encoding='utf-8') as f:
                            contents = f.read()
                            if re.search("import\s+pytest", contents):
                                logger.debug('the file have import pytest in it')
                                correct = True

                if not correct:
                    # not a pytest file
                    logger.debug('no "test" in filename {} (path: {}) and no "import pytest" in file'.format(testfile_name, testfile))
                    continue

                # new tester version accepts test separately
                test_list = []

                results_count = 0
                results_passed = 0
                results_failed = 0
                results_skipped = 0
                # for weighted tests
                # whether different weights are used
                has_different_weights = False
                weight_count = 0
                weight_passed = 0
                weight_failed = 0
                weight_skipped = 0
                try:
                    # let's remove pytest out file so that the previous file won't be used in case the test fails due to timeout
                    os.remove(pytest_output_file)
                except OSError:
                    pass

                # socket disabled?
                disable_socket = '--disable-socket'
                if 'enable_socket' in extra:
                    logger.debug("SOCKET ENABLED!")
                    disable_socket = ''

                cmd = 'timeout {} pytest --json={} --junitxml={} --durations=10 --timeout_method=signal {} "{}"'.format(timeout, pytest_output_file, pytest_output_xml, disable_socket, testfile)

                (exitval, out, err, _) = sh(cmd)
                logger.debug("Executed: " + cmd)
                logger.debug("sterr: " + err)
                logger.debug("stdout: " + out)
                logger.debug('return code:' + str(exitval))
                # results_output = ""
                testname = os.path.basename(testfile)
                results_output += "Test: {}\n".format(testname)
                try:
                    logger.debug('reading output file:' + pytest_output_file)
                    pytest_data = json.load(open(pytest_output_file, 'r', encoding='utf-8'))
                    logger.debug('contents:' + str(pytest_data))
                    if 'report' in pytest_data:
                        # "summary": {"duration": 0.036809444427490234, "num_tests": 3, "passed": 1, "failed": 2},
                        if 'summary' in pytest_data['report']:
                            summary_data = pytest_data['report']['summary']
                            if 'num_tests' in summary_data:
                                results_count = summary_data['num_tests']
                            if 'passed' in summary_data:
                                results_passed = summary_data['passed']
                            if 'failed' in summary_data:
                                results_failed = summary_data['failed']
                            if 'skipped' in summary_data:
                                results_skipped = summary_data['skipped']
                        if 'tests' in pytest_data['report']:
                            """
                             [{'name': 'bomber_tests.py::test_stronger_win[strong4]',
                             'run_index': 19,
                             'teardown': {'duration': 0.00027680397033691406, 'name': 'teardown', 'outcome': 'passed'},
                             'duration': 0.0011937618255615234,
                             'setup': {'duration': 0.00032448768615722656, 'name': 'setup', 'outcome': 'passed'},
                             'outcome': 'failed', 'call': {'duration': 0.00026798248291015625, 'name': 'call', 'outcome': 'failed',
                             'longrepr': "gameid = 'strong4'\n\n
                             @pytest.mark.parametrize('gameid', ['strong' + str(x) for x in range(5)])\n
                             def test_stronger_win(gameid):\n
                             b1 = StrongerBomberman('W')\n
                             b2 = StudentBomberman('S')\n
                             result = _test_game(gameid, b1, b2)\n
                             >       assert result == ['S']\n
                             E       assert ['W'] == ['S']\n
                             E         At index 0 diff: 'W' != 'S'\n
                             E         Use -v to get the full diff\n\n
                             bomber_tests.py:63: AssertionError"}}
                            """
                            # let's try to sort by run_index
                            try:
                                sorted_tests = sorted(pytest_data['report']['tests'], key=lambda x:x['run_index'])
                            except:
                                # fallback just in case
                                sorted_tests = pytest_data['report']['tests']
                            for testdata in sorted_tests:
                                # single test output
                                test_name = ''
                                test_result = ''
                                test_duration = 0
                                test_duration_str = ''
                                test_output = ''
                                # duration
                                if 'duration' in testdata:
                                    try:
                                        dur = float(testdata['duration'])
                                        if dur < 1.0:
                                            test_duration_str = "{:.4} ms".format(dur * 1000)
                                        else:
                                            test_duration_str = "{:.2} s".format(dur)

                                        test_duration = int(1000 * float(testdata['duration']))
                                    except:
                                        pass
                                weight = 1
                                if 'metadata' in testdata:
                                    #  "metadata": [{"weight": 3}]
                                    if isinstance(testdata['metadata'], list):
                                        try:
                                            weight = testdata['metadata'][0]['weight']
                                            if weight != 1:
                                                has_different_weights = True
                                        except:
                                            pass
                                weight_count += weight
                                if testdata['outcome'] == 'passed':
                                    weight_passed += weight
                                if testdata['outcome'] == 'failed':
                                    weight_failed += weight
                                if testdata['outcome'] == 'skipped':
                                    weight_skipped += weight


                                tokens = testdata['name'].split('::')

                                if len(tokens) == 2:
                                    test_name = tokens[1]
                                    test_result = testdata['outcome']
                                    #results_output += "\n" + tokens[1] + ": " + testdata['outcome'] + "\n"
                                if testdata['outcome'] == 'failed' and 'call' in testdata:
                                    if testdata['call']['outcome'] == 'failed':
                                        failed_message = testdata['call']['longrepr']
                                        logger.debug('Fail message:\n' + failed_message)
                                        for line in failed_message.split('\n'):
                                            if len(line) > 1 and line[0] == 'E' and line[1] in (' ', '\t'):
                                                # dont include assert errors
                                                if 'assert' in line.lower(): break
                                                #results_output += "  " + line[1:]
                                                test_output += "  " + line[1:] + "\n"
                                if test_name and test_result:
                                    if test_result == 'failed': test_result = 'FAILED'
                                    test_weight = ""
                                    if weight != 1:
                                        test_weight = " weight: {}".format(weight)

                                    if test_duration_str:
                                        test_duration_str = ' ({})'.format(test_duration_str)
                                    if test_output:
                                        test_output = '   {}\n'.format(test_output)
                                    results_output += "\n   {}: {}{}{}\n{}".format(test_name, test_result, test_duration_str, test_weight, test_output)
                                    test_list.append({'name': test_name,
                                                      'status': test_result.upper(),
                                                      'weight': weight,
                                                      'description': test_output,
                                                      'timeElapsed': test_duration})


                    if results_count == 0:
                        results_output += "\nErrors in the code.\nCheck the file name, check the module/package name, check the function/method names."
                    if results_count >= 0:
                        results_output += "\n\nTotal number of tests: {}\n".format(results_count)
                        results_output += "Passed tests: {}\n".format(results_passed)
                        results_output += "Failed tests: {}\n".format(results_failed)
                        if results_skipped > 0:
                            results_output += "Skipped tests: {}\n".format(results_skipped)
                        results_percent = 0
                        if results_count > 0:
                            results_percent = results_passed / results_count

                        if has_different_weights:
                            # show information about weighted test results
                            results_output += "Passed tests weight: {}\n".format(weight_passed)
                            results_output += "Failed tests weight: {}\n".format(weight_failed)
                            if weight_skipped:
                                results_output += "Skipped tests weight: {}\n".format(weight_skipped)
                            if weight_count > 0:
                                results_percent = weight_passed / weight_count
                            # for total calculation
                            results_count = weight_count
                            results_passed = weight_passed

                        results_output += "\nPercentage: {:.2%}\n\n".format(results_percent)

                        results_total_count += results_count
                        results_total_passed += results_passed
                    else:
                        # possible error, let's check xml output
                        # as this mainly applies for test errors, no point to show those.
                        tree = ET.parse(pytest_output_xml)
                        root = tree.getroot()
                        error_message = root[0][0].text
                        """
                        for line in error_message:
                            if line[0] == 'E':
                                # let's check only "E" lines
                                pass
                        """
                        logger.debug('error message from XML:\n' + error_message)
                        pass
                    testfile_list.append({'name': str(testfile), 'file': str(testfile), 'count': results_count, 'passedCount': results_passed, 'unitTests': test_list,
                                          'identifier': grade_number, 'grade': results_percent * 100})
                    grade_number += 1

                except:
                    logger.exception("Error while parsing pytest output json")
                    pass


            if int(exitval) > 123:
                # timeout, let's build our own json
                d = {
                    'results': [
                        {'grade_type_code': 1, 'percentage': 0},
                        {'grade_type_code': 101, 'percentage': 0}
                    ],
                    'percent': 0,
                    'output': 'Programmi testimiseks lubatud aeg on möödas. Sinu programm töötas liiga kaua ja ei andnud vastust. Proovi oma programmi parandada.\n\nSession:' + str(session),
                    'files':[]
                }
                with open(resultfile, 'w', encoding='utf-8') as f:
                    json.dump(d, f)
            else:
                # no timeout
                results_total_percent = 0
                if results_total_count > 0:
                    results_total_percent = results_total_passed / results_total_count

                if email_feedback == 'minimal':
                    # send email info to extra
                    extra_output = results_output
                    results_output = "Submission received"

                results_list.append(
                    {'code': 500, "identifier": "TESTNG", "result": "SUCCESS", 'testContexts': testfile_list,
                     'totalGrade': results_total_percent * 100,
                     "totalCount": results_total_count,
                     "totalPassedCount": results_total_passed})
                # email text
                results_list.append({"code": 2147483647, "identifier": "REPORT", "output": results_output,
                                     "result": "SUCCESS"})
                d = {
                    "type": "hodor_studenttester",
                    'results': results_list,
                    #'output': results_output,
                    #'percent': results_total_percent * 100,
                    #'files': source_list,
                    'extra': extra_output
                }
                with open(resultfile, 'w', encoding='utf-8') as f:
                    json.dump(d, f)

        with open(resultfile, 'r', encoding='utf-8') as f:
            result_json = f.read()
            logger.debug('result:' + str(result_json))
            return str(result_json)
    except:
        logger.exception("Error while executing python tester")
    return None


if __name__ == '__main__':
    json_string = "".join(sys.stdin)
    result = test(json_string)
    print(result)