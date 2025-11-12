package com.demo.service;

import com.demo.common.Message;
import com.demo.model.DltMessageDTO;

import java.util.List;

/**
 * Service interface for managing Dead-Letter Topic (DLT) messages.
 * <p>
 * This service provides the core logic for the DLT service:
 * <ol>
 * <li>Registering (saving) new failed messages as they are consumed from DLTs.</li>
 * <li>Retrieving all saved messages for administrative review.</li>
 * </ol>
 *
 * @see com.demo.controller.ErrorController
 *
 *
 **/
public interface DltMessageService {

    /**
     * Registers a new, failed message to the DLT database.
     * <p>
     * This method is called by the DLT consumer (e.g., a Kafka listener)
     * when a message is received from a Dead-Letter Topic. It persists
     * the message's details for later inspection and manual intervention.
     *
     * @param message The raw, failed {@link Message} consumed from a DLT.
     */
    void register(Message message);

    /**
     * Retrieves all registered DLT messages.
     * <p>
     * This is used by the {@link com.demo.controller.ErrorController}
     * to provide a list of all failed messages to an administrator.
     *
     * @return A list of {@link DltMessageDTO}s representing all
     * failed messages currently stored in the database.
     */
    List<DltMessageDTO> retrieveAll();

}
