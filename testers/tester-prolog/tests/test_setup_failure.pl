:- use_module('eplunit').

:- begin_tests(block_setup_failure, [setup(fail)]).

test(test1) :- true.
test(test2) :- true.

:- end_tests(block_setup_failure).

:- begin_tests(test_setup_failure).

test(test3, setup(fail)) :- true.
test(test4, setup(true)) :- true.

:- end_tests(test_setup_failure).
