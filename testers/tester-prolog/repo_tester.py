import json
from os import path
from git import Repo
from new_tester import test as new_test
from tester import test as current_test
from collections import defaultdict
import csv

git_repo = "/home/ss/projects/prolog"
tests_folder = "/home/ss/projects/prolog_tester/tests/"

repo = Repo(git_repo)
commits = list(repo.iter_commits())
commits.reverse()

testable_commits = 0
tests_run = 0
number_of_tests = [2, 24, 10, 8, 28, 3, 24, 8, 4, 8, 4, 3]

def run_new_tester(json_string):
    output = new_test(json_string)
    result = json.loads(output)["output"]
    failed = 0
    blocked = 0
    passed = 0
    failed_start = result.find("Failed tests:")
    blocked_start = result.find("Blocked tests:")
    passed_start = result.find("Passed tests:")
    if failed_start > -1:
        end = len(result)
        if blocked_start > -1:
            end = blocked_start
        elif passed_start > -1:
            end = passed_start
        failed = result.count("(weight: ", failed_start, end)
    if blocked_start > -1:
        end = len(result)
        if passed_start > -1:
            end = passed_start
        blocked = result.count("(weight: ", blocked_start, end)
    if passed_start > -1:
        passed = result.count("(weight: ", passed_start)
    return passed, failed, blocked

def run_current_tester(json_string):
    output = current_test(json_string)
    result = json.loads(output)["results"][0]["output"]
    passed_tests = 0
    passed_tests_separator = "Total tests passed: "
    passed_tests_start = result.find(passed_tests_separator)
    if passed_tests_start > -1:
        passed_tests_start += len(passed_tests_separator)
        passed_tests_end = result.find('/', passed_tests_start)
        passed_tests = int(result[passed_tests_start:passed_tests_end])
    failed_tests = 0
    failed_tests_separator = "Failed tests: \n"
    failed_tests_start = result.find(failed_tests_separator)
    if failed_tests_start > -1:
        failed_tests_start += len(failed_tests_separator)
        failed_tests = len(result[failed_tests_start:].split())
    return passed_tests, failed_tests

silent_failures = 0

results_current = [defaultdict(lambda : 0) for _ in range(12)]
results_new = [defaultdict(lambda : 0) for _ in range(12)]

try:
    for commit in commits:
        to_test = []
        for file_name in commit.stats.files:
            if file_name[:2] == 'PR':
                tail, _ = path.split(file_name)
                to_test.append(tail)

        if to_test:
            print(to_test)
            tests_run += len(to_test)
            testable_commits += 1
            repo.head.reference = repo.commit(commit)
            repo.head.reset(working_tree=True)
            for folder in to_test:
                testing_data = {
                    "contentRoot": path.join(git_repo, folder),
                    "testRoot": path.join(tests_folder, folder)
                }
                json_string = json.dumps(testing_data)
                test_nr = int(folder[2:]) - 1
                tests = number_of_tests[test_nr]
                passed_tests_new, failed_tests_new, blocked_tests_new = run_new_tester(json_string)
                results_current[test_nr]["runs"] += 1
                results_current[test_nr]["passed"] += passed_tests_new
                results_current[test_nr]["failed"] += failed_tests_new
                results_current[test_nr]["blocked"] += blocked_tests_new
                results_current[test_nr]["silently failed"] += tests - blocked_tests_new - passed_tests_new - failed_tests_new
                # print(json_string)
                # passed_tests, failed_tests = run_current_tester(json_string)
                # results_current[test_nr]["runs"] += 1
                # results_current[test_nr]["passed"] += passed_tests
                # results_current[test_nr]["failed"] += failed_tests
                # results_current[test_nr]["silently failed"] += tests - passed_tests - failed_tests
                silent = tests - blocked_tests_new - passed_tests_new - failed_tests_new
                print(test_nr, tests, passed_tests_new, failed_tests_new, blocked_tests_new, silent)
finally:
    repo.heads.master.checkout()
    print(len(commits), testable_commits, tests_run)
    print(results_current)
    # with open("current_results.csv", 'w') as f:
    #     w = csv.DictWriter(f, ["runs", "passed", "failed", "silently failed"], extrasaction='ignore')
    #     w.writeheader()
    #     w.writerows(results_current)
    with open("new_results.csv", 'w') as f:
        w = csv.DictWriter(f, ["runs", "passed", "failed", "blocked", "silently failed"], extrasaction='ignore')
        w.writeheader()
        w.writerows(results_current)
