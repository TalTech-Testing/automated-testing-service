from setuptools import setup


setup(
    name="Hodor Python tester",
    version="0.1",
    description="Python (pytest) tester for Hodor",
    install_requires=[
        "pytest",
        "pytest-json",
        "pytest-timeout",
        "pytest-socket",
        "pep257",
        "mock",
        "pytest-console-scripts",
        "flake8"
    ]
)