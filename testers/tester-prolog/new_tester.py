import subprocess, json, csv, sys, os
from collections import OrderedDict
from io import StringIO
from shutil import rmtree, copy2, copystat, Error
from re import match
from time import sleep

def sh(cmd):
   """
   Executes the command.
   Returns (returncode, stdout, stderr, Popen object)
   """
   p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
   out, err = p.communicate()
   out = out.decode("utf-8")
   err = err.decode("utf-8")
   return (p.returncode, out, err, p)

output_format = """  {unit}-{name}: {description} (weight: {weight})
    {result}
"""

def copyfiles(src, dst, ignore=None):
   """
   https://docs.python.org/2/library/shutil.html#copytree-example
   with some modifications (destination folder can exist)
   """
   names = os.listdir(src)
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

testing_path = "/tmp/prolog-tester"
main_test_re = "test_pr.*\.pl"
csv_separator = "START_SIMPLE_REPORT\n"

def main_tests(files):
    for file_name in files:
        file_name = os.path.relpath(file_name, testing_path)
        if match(main_test_re, file_name):
            yield file_name

def test(json_string):
    try:
        rmtree(testing_path)
    except FileNotFoundError:
        pass
    input_data = json.loads(json_string)

    output = { "results": [], "version": 1 }

    source = []
    for path, _, files in os.walk(input_data["contentRoot"]):
        for file_name in files:
            source_file = {}
            file_path = os.path.join(path, file_name)
            source_file["path"] = file_path
            with open(file_path) as f:
                source_file["content"] = f.read()
            source.append(source_file)
    output["source"] = source

    content_files = copyfiles(input_data["contentRoot"], testing_path)
    test_files = copyfiles(input_data["testRoot"], testing_path)

    overwritten_files = []
    for file_name in content_files:
        if file_name in test_files:
            overwritten_files.append(os.path.relpath(file_name, testing_path))

    if overwritten_files:
        message = "Some of the submitted code was overwritten by the tester. Please rename the following files: {}." \
            .format(", ". join(overwritten_files))
        output["percentage"] = 0
        output["output"] = message
        return json.dumps(output)

    total_points = 0
    total_granted = 0
    tests_output = ""
    results = []

    for grade_code, test_file in enumerate(main_tests(test_files), start=1):
        points = 0
        granted = 0
        test_output = ""
        _, out, err, _ = sh("cd {} && swipl -qg run_tests -t halt {}".format(testing_path, test_file))
        result = { "stdout": out, "stderr": err, "grade_type_code": grade_code, "name": test_file }

        test_output += test_file + "\n"
        if err:
            test_output += "Errors and warnings:\n" + err

        csv_start = out.rfind(csv_separator)
        if csv_start < 0:
            test_output += "{} was not found in the tester output.\n".format(repr(csv_separator))
        else:
            csv_start += len(csv_separator)

            result_reader = csv.reader(StringIO(out[csv_start:]))
            test_results = OrderedDict()
            for row in result_reader:
                test_result = row[0]
                if test_result == 'Fixme':
                    if row[-1] == 'passed':
                        test_result = 'Passed'
                    else:
                        test_result = 'Failed'
                    row = row[:-1]
                list = test_results.get(test_result, [])
                list.append(row[1:])
                test_results[test_result] = list

            for category, tests in test_results.items():
                test_output += "\n{} tests:\n".format(category)
                for test in tests:
                    test_result = test[-1]
                    test_weight = int(test[3])
                    points += test_weight
                    if category == 'Passed':
                        granted += test_weight
                        test_result = 'passed in {} seconds'.format(test[-1])
                    test_output += output_format.format(unit=test[0], name=test[1],
                        description=test[2], weight=test_weight, result=test_result)

        if points == 0:
            test_output += "No tests were run.\n"
            result["percentage"] = 0
        else:
            result["percentage"] = 100.0 * granted / points
        result["output"] = test_output
        total_points += points
        total_granted += granted
        tests_output += test_output
        results.append(result)
    output["results"] = results
    output["output"] = tests_output
    if total_points == 0:
        output["percentage"] = 0
    else:
        output["percentage"] = 100.0 * total_granted / total_points
    return json.dumps(output)

if __name__ == '__main__':
    json_string = "".join(sys.stdin)
    print(test(json_string))
