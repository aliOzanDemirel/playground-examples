package common

import (
	"os"
	"testing"
	"time"
)

func TestReadEnvVar(t *testing.T) {

	t.Run("string list with default", func(t *testing.T) {
		varVal := ReadEnvStringList("string_list_test_env", nil)
		if len(varVal) != 0 {
			t.Errorf("unexpected value: %v", varVal)
		}
	})
	t.Run("string with env var value", func(t *testing.T) {
		_ = os.Setenv("string_list_test_env", "env_str_1|env_str_2")
		varVal := ReadEnvStringList("string_list_test_env", nil)
		if len(varVal) != 2 {
			t.Errorf("unexpected value: %v", varVal)
		}
	})

	t.Run("string with default", func(t *testing.T) {
		varVal := ReadEnvString("string_env", "default_str_value")
		if varVal != "default_str_value" {
			t.Errorf("unexpected value: %s", varVal)
		}
	})
	t.Run("string with env var value", func(t *testing.T) {
		_ = os.Setenv("string_env", "env_str_value")
		varVal := ReadEnvString("string_env", "default_str_value")
		if varVal != "env_str_value" {
			t.Errorf("unexpected value: %s", varVal)
		}
	})

	t.Run("bool with default", func(t *testing.T) {
		varVal := ReadEnvBool("bool_env", true)
		if varVal != true {
			t.Errorf("unexpected value: %t", varVal)
		}
	})
	t.Run("bool with env var value", func(t *testing.T) {
		_ = os.Setenv("bool_env", "true")
		varVal := ReadEnvBool("bool_env", false)
		if varVal != true {
			t.Errorf("unexpected value: %t", varVal)
		}
	})

	t.Run("duration with default", func(t *testing.T) {
		defValue := time.Duration(10) * time.Second
		varVal := ReadEnvDuration("duration_env", defValue)
		if varVal != defValue {
			t.Errorf("unexpected value: %v", varVal)
		}
	})
	t.Run("duration with env var value", func(t *testing.T) {
		_ = os.Setenv("duration_env", "3m")
		varVal := ReadEnvDuration("duration_env", 0)
		if varVal != time.Duration(3)*time.Minute {
			t.Errorf("unexpected value: %v", varVal)
		}
	})

	t.Run("uint with default", func(t *testing.T) {
		varVal := ReadEnvUint("test_env", 1)
		if varVal != 1 {
			t.Errorf("unexpected value: %d", varVal)
		}
	})
	t.Run("uint with env var value", func(t *testing.T) {
		_ = os.Setenv("uint_env", "9999")
		varVal := ReadEnvUint("uint_env", 0)
		if varVal != 9999 {
			t.Errorf("unexpected value: %d", varVal)
		}
	})
}

func TestPrefixedEnvKeyValues(t *testing.T) {

	_ = os.Setenv("prefix_test_env1", "1")
	_ = os.Setenv("prefix_test_env2", "2")
	_ = os.Setenv("test_env3", "3")
	_ = os.Setenv("prefixtestexclude_env4", "4")

	envVars := PrefixedEnvKeyValues("prefix_test_")
	if len(envVars) != 2 {
		t.Fatalf("unexpected count, actual: %d expected: %d", len(envVars), 2)
	}

	_, foundEnv1 := envVars["env1"]
	_, foundEnv2 := envVars["env2"]
	if !foundEnv1 || !foundEnv2 {
		t.Fatalf("did not find expected env vars")
	}
}
