class stratos {

	stage { 'configure': require => Stage['main'] }
	stage { 'deploy': require => Stage['configure'] }
}

import "subclasses/*"
