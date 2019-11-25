:- use_module('eplunit').

:- begin_tests(final, [dependson([basic, advanced])]).
test(final_blocked) :- true.
:- end_tests(final).

:- begin_tests(advanced, [dependson([basic])]).
test(advanced_failure) :- fail.
:- end_tests(advanced).

:- begin_tests(basic).
test(basic_success) :- true.
:- end_tests(basic).

% fails due to cyclic dependency
% :- begin_tests(loop, [dependson([loop])]).
% test(advanced_success) :- true.
% :- end_tests(loop).

% fails due to nonexistent dependency
% :- begin_tests(invalid_dependency, [dependson([invalid])]).
% test(advanced_success) :- true.
% :- end_tests(invalid_dependency).
