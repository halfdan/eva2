#!/usr/bin/env ruby
sum = 0
ARGV.each do |val|
	sum = sum + (val.to_f * val.to_f)
end
puts sum
