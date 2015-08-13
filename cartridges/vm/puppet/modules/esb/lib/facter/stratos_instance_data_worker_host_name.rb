Facter.add("stratos_instance_data_worker_host_name") do  
	setcode do      
		Facter::Util::Resolution.exec('esb.wso2.com')  
	end
end
