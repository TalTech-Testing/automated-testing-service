:- use_module('eplunit').

:- begin_tests(generic).

test(add_pass, true(A =:= 3)) :-
    A is 1 + 2.

test(add_fail, true(A =:= 3)) :-
    A is 1 + 1.

test(add_fail_2, true(3 =:= A)) :-
    A is 1 + 1.

:- end_tests(generic).
