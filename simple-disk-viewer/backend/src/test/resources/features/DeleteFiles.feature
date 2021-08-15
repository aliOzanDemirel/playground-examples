Feature: Delete files that are not directory

  Background:
    Given temporary directory "temp"
    Given temporary directory has folder "child1"
    Given temporary directory has file "file1.txt" under "child1"

  Scenario:
    When file "temp" is requested to be deleted
    Then response status is 204
    Then file "temp/child1/file1.txt" does not exist
    Then file "temp/child1" does not exist

  Scenario:
    When file "temp/child1/file1.txt" is requested to be deleted
    Then response status is 204
    Then file "temp/child1/file1.txt" does not exist
