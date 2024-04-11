package common

import (
	"regexp"
	"testing"
	"time"
)

func TestSleepDurationWithBackoff(t *testing.T) {

	sleepBase := 3 * time.Second
	newSleepDuration := SleepDurationWithBackoff(sleepBase)
	if newSleepDuration < sleepBase {
		t.Fatalf("unexpected sleep duration: %v", newSleepDuration)
	}
}

func TestUniqueId(t *testing.T) {

	idConstraint := regexp.MustCompile(`^[a-zA-Z0-9_-]{0,32}$`)
	id := UniqueId()
	matched := idConstraint.FindStringIndex(id)
	if matched == nil {
		t.Fatalf("failed regex constraint: %s", idConstraint.String())
	}
	if matched[0] != 0 && matched[1] != 32 {
		t.Fatalf("unexpected matched string index: %d", matched)
	}
}

func TestValidateHttpUrl(t *testing.T) {

	negativeUrls := [...]string{
		"http//crypto.com",
		"https:crypto.com",
		"http:/no.host",
		"no.scheme.com/fails",
		"/path/fails",
		"http://valid.url.with.only.separator:443/",
		"https://valid.url:443/any/path/fails",
	}
	for _, tURL := range negativeUrls {
		t.Run("invalid url", func(t *testing.T) {
			err := ValidateHttpUrl(tURL, true)
			if err == nil {
				t.Errorf("URL should have failed validation, '%v'", tURL)
			}
		})
	}

	positiveUrls := [...]string{
		"http://crypto.com",
		"https://CRYPTO.COM.WITH.PORT:8080",
	}
	for _, tURL := range positiveUrls {
		t.Run("valid url with no path", func(t *testing.T) {
			err := ValidateHttpUrl(tURL, true)
			if err != nil {
				t.Errorf("URL should have passed validation, '%v'", tURL)
			}
		})
	}

	positiveUrlsWithPaths := [...]string{
		"http://crypto.com",
		"http://crypto.com/",
		"http://crypto.com/some/path",
	}
	for _, tURL := range positiveUrlsWithPaths {
		t.Run("valid url with path", func(t *testing.T) {
			err := ValidateHttpUrl(tURL, false)
			if err != nil {
				t.Errorf("URL should have passed validation, '%v'", tURL)
			}
		})
	}
}
