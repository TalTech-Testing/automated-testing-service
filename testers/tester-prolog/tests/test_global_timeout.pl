:- use_module('eplunit').

:- set_test_options([timeout(5)]).

:- begin_tests(timeout).

test(timeout_default) :- sleep(4).
test(timeout_new) :- sleep(6).

:- end_tests(timeout).
