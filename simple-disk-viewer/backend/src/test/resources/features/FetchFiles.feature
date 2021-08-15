Feature: Fetch files and file system roots

  Background:
    Given temporary directory "temp"
    Given temporary directory has folder "child1"
    Given temporary directory has file "file1.txt" under "child1"
    Given temporary directory has folder "child2"
    Given temporary directory has file "file2.pdf" under "child2"
    Given temporary directory has folder "child2\child2-1"
    Given temporary directory has file "file2-1.txt" under "child2\child2-1"
    Given temporary directory has folder "child2\child2-2\child2-2-1\child2-2-1-1"

  Scenario: fetch file system roots
    When file system roots is requested
    Then response status is 200
    And response list should have "C:\"

  Scenario: fetch file details of temporary directory
    When file details of "temp" is requested with depth 2
    Then response status is 200
    And file response has parent "temp" that has child "temp/child1"
    And file response has parent "temp/child1" that has child "temp/child1/file1.txt"
    And file response has parent "temp" that has child "temp/child2"
    And file response has parent "temp/child2" that has child "temp/child2/file2.pdf"
    And file response does not have "temp/child2/child2-1/file2-1.txt"

  Scenario: fetch file details of temporary directory with bigger depth
    When file details of "temp/child2" is requested with depth 3
    Then response status is 200
    And file response has parent "temp/child2" that has child "temp/child2/file2.pdf"
    And file response has parent "temp/child2" that has child "temp/child2/child2-1"
    And file response has parent "temp/child2/child2-1" that has child "temp/child2/child2-1/file2-1.txt"
    And file response has parent "temp/child2/child2-2/child2-2-1" that has child "temp/child2/child2-2/child2-2-1/child2-2-1-1"

  Scenario: fetch file details of temporary directory
    When file details of "temp/child2/file2.pdf" is requested with depth 1
    Then response status is 200
    And file response has parent "temp/child2/file2.pdf" that has child ""
