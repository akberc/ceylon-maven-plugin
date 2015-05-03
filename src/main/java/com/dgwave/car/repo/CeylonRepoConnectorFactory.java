package com.dgwave.car.repo;

import javax.inject.Named;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnector;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.locator.Service;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.spi.log.Logger;
import org.eclipse.aether.spi.log.NullLoggerFactory;
import org.eclipse.aether.transfer.NoRepositoryConnectorException;

@Named
@Component(role=RepositoryConnectorFactory.class, hint="ceylon")
public final class CeylonRepoConnectorFactory implements RepositoryConnectorFactory, Service {

	private Logger logger;

	@Override
	public RepositoryConnector newInstance(RepositorySystemSession session,
			RemoteRepository repository) throws NoRepositoryConnectorException {
		if (!"ceylon".equals(repository.getId())) {
			throw new NoRepositoryConnectorException(repository);
		}
		
		return new CeylonRepositoryConnector(logger);
	}

	@Override
	public float getPriority() {
		return 0;
	}

	@Override
	public void initService(ServiceLocator locator) {
		setLogger( locator.getService( Logger.class ) );
	}

	private CeylonRepoConnectorFactory setLogger(Logger logger) {
        this.logger = ( logger != null ) ? logger : NullLoggerFactory.LOGGER;
        return this;
	}
}